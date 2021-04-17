import base.BaseCard;
import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.*;
import base.skat.Card;
import base.skat.messages.*;
import com.google.gson.JsonArray;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SkatServer extends BaseServer{


    private int beginner =0;
    private int hoeren =0;
    private int sagen = 0;
    private int weitersagen =0;
    private int currentPlayer = 0;
    private GameSelected.GAMES selectedGame = GameSelected.GAMES.Ramsch;
    private boolean hand;
    private boolean ouvert;

    private int spectator=3;

    private int currentGameValue = 0;
    private Random random;
    private List<BaseCard> skat;
    private int nextRamschPlayer;

    public SkatServer(BaseServer server) {
        super(server.c, server.comServer);
        server.comServer.setServer(this);
        this.players.addAll(server.players);
        gameType = Statics.game.SKAT;
    }

    @Override
    public void handleInput(MessageIn message) {
        super.handleInput(message);
        RequestObject requestObject = RequestObject.fromString(message.getInput());
        Socket socketConnection = message.getSocket();
        players.stream().filter(player -> player.getSocket()==socketConnection).findFirst().ifPresent(
                player -> log.info("Received: " + requestObject.getCommand() + " from " + player.getName()));
        switch (requestObject.getCommand()) {
            case PutCard.COMMAND:
                break;
            case Reizen.COMMAND:
                handleReizen(requestObject);
                break;
            case Passen.COMMAND:
                handlePassen(requestObject);
                break;
            case GetSkat.COMMAND:
                handleGetSkat(requestObject);
                break;
            case Skat.COMMAND:
                handleSkat(requestObject);
                break;
            case Schieben.COMMAND:
                handleSchieben();
                break;
            case GameSelected.COMMAND:
                handleGameSelected(requestObject);
                break;
        }
    }

    private void handleGameSelected(RequestObject message) {
        selectedGame = GameSelected.GAMES.valueOf(message.getParams().get("game").getAsString());
        hand = message.getParams().get("hand").getAsBoolean();
        ouvert = message.getParams().get("ouvert").getAsBoolean();
        send2All(new DisplayMessage("Spiel: " +selectedGame.name() + (hand ? " hand":"" )+ (ouvert ? " ouvert":"")));
        send2All(message);
    }

    private void handleSchieben() {
        send2All(new DisplayMessage("Der Skat wurde geschoben"));
        queueOut(players.get(nextRamschPlayer),new RamschSkat());
        if(nextRamschPlayer==sagen){
            nextRamschPlayer= weitersagen;
        }
        else{
            send2All(new Wait4Player(players.get(beginner).getName()));
        }
    }

    private void handleSkat(RequestObject message) {
        JsonArray array = message.getParams().getAsJsonArray("cards");
        skat = new ArrayList<>();
        array.forEach(card->{
            base.doko.Card c = new base.doko.Card(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            skat.add(c);
        });
        //TODO: remove returned cards from players hand
        if(selectedGame== GameSelected.GAMES.Ramsch){
            send2All(new DisplayMessage(players.get(nextRamschPlayer).getName() +" bekommt den Skat"));
            queueOut(players.get(nextRamschPlayer),new RamschSkat());
            if(nextRamschPlayer==sagen){
                nextRamschPlayer= weitersagen;
            }
            else{
                send2All(new Wait4Player(players.get(beginner).getName()));
            }
        }
    }

    private void handleGetSkat(RequestObject message) {
        queueOut(players.stream().filter(player -> player.getName().equals(message.getParams().get("player").getAsString())).findFirst().get(),
                new Skat(skat));
        send2All(new DisplayMessage(players.stream().filter(player -> player.getName().equals(message.getParams()
                .get("player").getAsString())).findFirst().get().getName() + " nimmt den Skat auf"));
        players.stream().filter(player -> player.getName().equals(message.getParams()
                .get("player").getAsString())).findFirst().get().getHand().addAll(skat);
        skat.clear();
    }

    private void handlePassen(RequestObject message) {
        send2All(new DisplayMessage(message.getParams().get("player").getAsString() + ": weg"));
        Optional<Player> player = players.stream().filter(p-> p.getName().equals(message.getParams().get("player").getAsString())).findFirst();
        if(player.isPresent()){
            int p = player.get().getNumber();
            if(p==hoeren){
                hoeren = -1;
                if(currentGameValue==0){
                    ramsch();
                }
                else if(sagen < 0){
                    selectGame(weitersagen);
                }
                else{
                    askNext(weitersagen);
                }
            }
            else if(p==weitersagen){
                weitersagen = -1;
                if(hoeren<0){
                    selectGame(sagen);
                }
                else if(sagen<0){
                    askNext(hoeren);
                }
            }
            else if(p==sagen){
                sagen = -1;
                if(hoeren < 0){
                    selectGame(weitersagen);
                }
                else{
                    askNext(weitersagen);
                }
            }
            else{
                log.error("Da lief etwas falsch");
            }
        }
    }

    private void ramsch() {
        send2All(new DisplayMessage("Ramsch: " + players.get(beginner).getName() + " bekommt den Skat"));
        queueOut(players.get(beginner),new RamschSkat());
        nextRamschPlayer = sagen;
    }

    private void selectGame(int playerNumber) {
        queueOut(players.get(playerNumber),new SelectGame());
    }

    private void askNext(int playerNumber) {
        queueOut(players.get(playerNumber),new Reizen(
                players.get(playerNumber).getName(),
                currentGameValue,
                true));
    }

    private void handleReizen(RequestObject message) {
        Optional<Player> player = players.stream().filter(p-> p.getName().equals(message.getParams().get("player")
                .getAsString())).findFirst();
        if(player.isPresent()) {
            int p = player.get().getNumber();
            if (message.getParams().get("active").getAsBoolean()) {
                currentGameValue = message.getParams().get("value").getAsInt();
                send2All(new DisplayMessage(
                        String.format("%s sagt %s",
                                message.getParams().get("player").getAsString(),
                                currentGameValue)));
                if (p == hoeren) {
                    selectGame(hoeren);
                } else if (p == sagen) {
                    queueOut(players.get(hoeren), new Reizen(players.get(hoeren).getName(), currentGameValue, false));
                } else if (p == weitersagen) {
                    if (sagen < 0) {
                        queueOut(players.get(sagen), new Reizen(players.get(sagen).getName(), currentGameValue, false));
                    } else if (hoeren < 0) {
                        queueOut(players.get(sagen), new Reizen(players.get(sagen).getName(), currentGameValue, false));
                    }
                }
            } else {
                send2All(new DisplayMessage(
                        String.format("%s sagt %s",
                                message.getParams().get("player").getAsString(),
                                "Ja")));
                if (p == hoeren) {
                    if(sagen<0){
                        queueOut(players.get(weitersagen), new Reizen(players.get(weitersagen).getName(), currentGameValue, true));
                    }
                    else{
                        queueOut(players.get(sagen), new Reizen(players.get(sagen).getName(), currentGameValue, true));
                    }
                } else if (p == sagen) {
                    queueOut(players.get(weitersagen), new Reizen(players.get(weitersagen).getName(), currentGameValue, true));
                }
            }
        }
    }


    private void shuffleCards() {
        try {
        /*for (Player player1 : players) {
            send2All(new UpdateUserPanel(player1.getName(), ""));
        }

         */


            //stichList = new ArrayList<>();
            random = new Random(System.currentTimeMillis());
            List<Card> cardList = Card.createCardList();


            players.forEach(player -> {
                player.setHand(new ArrayList<>());
                if (!player.isSpectator()) {
                    for (int i = 0; i < 10; i++) {
                        Card card = cardList.get(random.nextInt(cardList.size()));
                        player.getHand().add(card);
                        cardList.remove(card);
                    }
                }
            });
            skat = new ArrayList<>();
            skat.addAll(cardList);
            players.forEach(player -> queueOut(player,new Cards(player.getHand())));


            //TODO: Extract method to reset all game variables for a new game
            //wait4Gesund = true;
            //armutplayer =-1;
            //schwein = false;
            //gameSelection = new HashMap<>();
            //send2All(new SelectGame());
            hand= false;
            ouvert = false;
            setPlayerRoles();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void startGame() {
        super.startGame();
        shuffleCards();
    }

    private void reizen(Player player, boolean active){
        queueOut(player,new Reizen(player.getName(),currentGameValue,active));
    }

    private void setPlayerRoles(){
        hoeren = beginner;
        sagen = nextNotSpectator(hoeren);
        weitersagen = nextNotSpectator(sagen);
        queueOut(players.get(sagen),new Reizen(players.get(sagen).getName(),currentGameValue,true));
    }

    private int nextNotSpectator(int s) {
        int i=0;
        while(i<players.size()){
            i++;
            if((i+s)>=players.size()){
                s -=players.size();
            }
            if(!players.get(i+s).isSpectator()){
                return i+s;
            }
        }
        return -1;
    }

}
