import base.*;
import base.doko.DokoCards;
import base.doko.SortHand;
import base.doko.messages.MessageGameEnd;
import base.doko.messages.MessageGameSelected;
import base.doko.messages.MessageSchweinExists;
import base.doko.messages.MessageSelectGame;
import base.messages.*;

import javax.swing.*;
import java.awt.*;
import java.security.PublicKey;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static base.doko.messages.MessageGameSelected.GAMES.*;

public class FakeClient implements IInputputHandler {

    private final ComClient comClient;
    private final Configuration config;
    private MessageGameSelected.GAMES selectedGame = MessageGameSelected.GAMES.NORMAL;
    private int currentCardsOnTable = 0;
    private boolean schweinExists = false;
    private boolean wait4Player = false;
    private List<BaseCard> hand = new ArrayList<>();
    private String playerName;
    private List<String> players = new ArrayList<>();
    private int spectator;
    private BaseCard mustPlay;
    private HashMap<Integer, BaseCard> tableStich = new HashMap<>();
    private int aufspieler;
    private Map<String,List<BaseCard>> playerBuckets;
    private Map<String,List<BaseCard>> gameBuckets;


    private Logger log;

    public FakeClient(Configuration config, String playerName) {
        this.config = config;
        this.comClient = new ComClient(config.connection.server,config.connection.port, this,playerName);
        this.comClient.start();
        this.playerName = playerName;
        log = new Logger(playerName,4);
    }

    @Override
    public void handleInput(Message message) {
        //log.info(String.format("handled Message: %s",message.toJson()));
        switch (message.getCommand()){
            case MessageCards.COMMAND:
                handleCards(message);
                break;
            case MessagePutCard.COMMAND:
                handlePutCard(message);
                break;
            case MessageGameSelected.COMMAND:
                handleGameSelected(message);
                break;
            case MessageSelectGame.COMMAND:
                handleSelectGame(message);
                break;
            case MessageGameType.COMMAND:
                handleGameType(message);
                break;
            case MessageGameEnd.COMMAND:
                handleGameEnd(message);
                break;
            case MessageWait4Player.COMMAND:
                handleWait4Player(message);
                break;
            case MessagePlayerList.IN_LOBBY:{
                handlePlayersInLobby(message);
                break;
            }
        }
    }

    private void handleGameType(Message message) {
        MessageGameType messageGameType = new MessageGameType(message);
        selectedGame = messageGameType.getSelectedGame();
        if (hand != null && hand.size() > 0) {
            hand = SortHand.sort(hand,selectedGame,schweinExists);
            if (selectedGame==NORMAL
                    || selectedGame==KARO
                    || selectedGame==ARMUT) {
                if (hand.stream().filter(p -> p.suit.equals(Statics.KARO)
                        && p.kind.equals(Statics.ASS)).count() > 1) {
                    send(new MessageSchweinExists());
                    schweinExists = true;
                } else {
                    schweinExists = false;
                }
            }
        }
        aufspieler = -1;
    }

    private void handlePlayersInLobby(Message message) {
        MessagePlayerList messagePlayersInLobby = new MessagePlayerList(message);
        DefaultListModel<String> model = new DefaultListModel<>();
        players.clear();
        players.addAll(messagePlayersInLobby.getPlayerNamesList());
        model.addAll(players);
    }

    private void handleWait4Player(Message message) {
        if (message.getParams().get("player").getAsString().equals(playerName)) {
            Optional<BaseCard> optCard = Optional.empty();
            if (mustPlay != null) {
                if (mustPlay.trump && playerBuckets.get(Statics.TRUMPF).size() > 0) {
                    optCard = playerBuckets.get(Statics.TRUMPF).stream().findAny();
                } else if (playerBuckets.get(mustPlay.suit).size() > 0) {
                    optCard = playerBuckets.get(mustPlay.suit).stream().findAny();
                } else {
                    optCard = hand.stream().findAny();
                }
            } else {
                Decision decision = new Decision();
                gameBuckets.keySet().forEach(key-> {
                    if(!key.equals(Statics.TRUMPF)) {
                        decision.setSuit(key,gameBuckets.get(key).size() - playerBuckets.get(key).size());
                    }
                });
                for (Probability p: decision.getOrderedSuitList()) {
                    if(p.prob>2){
                       optCard = playerBuckets.get(p.suit).stream().filter(card -> card.kind.equals(Statics.ASS)).findFirst();
                       if (optCard.isPresent()){
                           log.info(String.format("ich spiele %s, weil noch %s verteilt sind.", optCard.get(),p.prob));
                           break;
                       }
                       else {
                           optCard = hand.stream().findAny();
                       }
                    }
                    else {
                        optCard = hand.stream().findAny();
                    }
                }
                if (!optCard.isPresent()){
                    optCard = hand.stream().findAny();
                }
            }
            optCard.ifPresent(card ->{
                send(new MessagePutCard(players.indexOf(playerName), card), true);
                log.info(String.format("played %s", card));
                hand.remove(card);
            });
            playerBuckets = DevideCards(hand);
        }
    }

    private void handleGameEnd(Message message) {
        currentCardsOnTable = 0;
        schweinExists = false;
        wait4Player = false;
        hand = new ArrayList<>();
        send(new MessageReadyForNextRound(players.indexOf(playerName)));
    }

    private void handleSelectGame(Message message) {
        send(new MessageGameSelected(players.indexOf(playerName), MessageGameSelected.GAMES.NORMAL));
    }

    private void handleGameSelected(Message message) {

    }

    private void handlePutCard(Message message) {
        try {
            int ownNumber = players.indexOf(playerName);
            List<Integer> tmpList = new ArrayList<>();
            int i = ownNumber;

            while (tmpList.size() < 4) {
                if (i != spectator) {
                    tmpList.add(i);
                }
                i++;
                if (i > players.size() - 1) {
                    i = 0;
                }
            }


            currentCardsOnTable++;
            MessagePutCard messagePutCard = new MessagePutCard(message);
            BaseCard card = messagePutCard.getCard(Statics.game.DOKO);
            card.trump = DokoCards.isTrumpf(card,selectedGame);

            removeCardFromBuckets(card);

            if(currentCardsOnTable ==1){
                mustPlay = card;
            }

            if (currentCardsOnTable > 3) {
                mustPlay = null;
                currentCardsOnTable = 0;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void removeCardFromBuckets(BaseCard card) {
        if (card.trump && gameBuckets.get(Statics.TRUMPF).size() > 0 && gameBuckets.get(Statics.TRUMPF).contains(card)) {
            gameBuckets.get(Statics.TRUMPF).remove(card);
        } else if (playerBuckets.get(card.suit).size() > 0 && gameBuckets.get(card.suit).contains(card)) {
            gameBuckets.get(card.suit);
        } else {
            log.error("card could not be removed");
        }
    }

    private void handleCards(Message message) {
        MessageCards messageCards = new MessageCards(message);
        selectedGame = MessageGameSelected.GAMES.NORMAL;
        hand = messageCards.getCards(Statics.game.DOKO);
        playerBuckets = DevideCards(hand);
        gameBuckets = DevideCards(DokoCards.ALL_CARDS);
    }


    private Map<String,List<BaseCard>> DevideCards(List<BaseCard> list){
        list.forEach(card -> card.trump = DokoCards.isTrumpf(card, selectedGame));
        List<BaseCard> localCardList = new ArrayList<>(list);
        Map<String,List<BaseCard>> buckets = new HashMap<>();


        buckets.put(Statics.TRUMPF,localCardList.stream().filter(card -> card.trump).collect(Collectors.toList()));
        buckets.get(Statics.TRUMPF).forEach(localCardList::remove);
        buckets.put(Statics.KREUZ,localCardList.stream().filter(card -> card.suit.equals(Statics.KREUZ)).collect(Collectors.toList()));
        buckets.get(Statics.KREUZ).forEach(localCardList::remove);
        buckets.put(Statics.PIK,localCardList.stream().filter(card -> card.suit.equals(Statics.PIK)).collect(Collectors.toList()));
        buckets.get(Statics.PIK).forEach(localCardList::remove);
        buckets.put(Statics.HERZ,localCardList.stream().filter(card -> card.suit.equals(Statics.HERZ)).collect(Collectors.toList()));
        buckets.get(Statics.HERZ).forEach(localCardList::remove);
        buckets.put(Statics.KARO,localCardList.stream().filter(card -> card.suit.equals(Statics.KARO)).collect(Collectors.toList()));
        buckets.get(Statics.KARO).forEach(localCardList::remove);
        log.info(String.format("buckets:\nTrumpf:%s\nKreuz:%s\nPik:%s\nHerz:%s\nKaro:%s\n",
                buckets.get(Statics.TRUMPF).size(),
                buckets.get(Statics.KREUZ).size(),
                buckets.get(Statics.PIK).size(),
                buckets.get(Statics.HERZ).size(),
                buckets.get(Statics.KARO).size()));
        return buckets;
    }


    private void send(Message message){
        send(message,false);
    }

    private void send(Message message, boolean delayed){
        if (delayed) {
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        comClient.queueOutMessage(message);
    }

    public static class Decision{

        List<Probability> list = new ArrayList<>();

        public void setSuit(String suit, int value){
            list.add(new Probability(suit, value));
        }

        public List<Probability> getOrderedSuitList(){
            Comparator<Probability> comparator = Comparator.comparing(probability -> probability.prob);
            list.sort(comparator);
            return list;
        }
    }

    public static class Probability{
        private String suit;
        private int prob;

        public Probability(String suit, int value) {
            this.suit=suit;
            this.prob=value;
        }
    }
}
