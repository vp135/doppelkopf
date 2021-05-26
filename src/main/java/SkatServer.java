import base.BaseCard;
import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.*;
import base.skat.Card;
import base.skat.Stich;
import base.skat.messages.*;
import com.google.gson.JsonArray;

import java.net.Socket;
import java.util.*;

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
    private int currentStichNumber =0;
    private Stich stich;
    private List<Stich> stichList = new ArrayList<>();
    private HashMap<Integer,Boolean> readyMap = new HashMap<>();
    private HashMap<Integer,Integer> points = new HashMap<>();
    private boolean wait4NextRound;
    private int gamesTilRamsch;
    private int gamesTilNormal;

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
        try {
            switch (requestObject.getCommand()) {
                case PutCard.COMMAND:
                    handlePutCard(requestObject);
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
                    handleSchieben(requestObject);
                    break;
                case GameSelected.COMMAND:
                    handleGameSelected(requestObject);
                    break;
                case ReadyForNextRound.COMMAND:
                    handleReadyForNextRound(requestObject);
                    break;
                case CurrentStich.LAST:
                    handleLastStich(requestObject);
                    break;
                case GrandHand.COMMAND:
                    handleGrandHand(requestObject);
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void handleGrandHand(RequestObject message) {
        Player player =  players.stream().filter(p->p.getName().equals(message.getParams().get("player").getAsString())).findFirst().get();
        if(message.getParams().get("grandHand").getAsBoolean()){
            selectedGame = GameSelected.GAMES.Grand;
            send2All(new GameSelected(player.getName(),selectedGame,true,false));
            hand = true;
            player.setRe(true);
            runGame(beginner);
            beginner--;
        }
        else{
            send2All(new DisplayMessage(String.format("%s spielt keinen Grand hand",player.getName())));
            askNextGrandHandPlayer();
        }
    }

    private void handleLastStich(RequestObject message) {
        if(stichList.size()>0) {
            try {
                CurrentStich cs = new CurrentStich(stichList.get(stichList.size()-1).getBaseCardMap(), CurrentStich.LAST);
                queueOut(players.get(message.getParams().get("player").getAsInt()), cs);
            }
            catch (Exception ex){
                log.warn(ex.toString());
            }
        }
    }

    private void handleReadyForNextRound(RequestObject message) {
        if(wait4NextRound){
            readyMap.put(message.getParams().get("player").getAsInt(),true);
        }
        if(readyMap.values().stream().allMatch(p-> p)){
            nextGame();
        }
    }

    private void handlePutCard(RequestObject requestObject) {
        if (stich == null || stich.getCardMap().size() > 2) {
            stich = new Stich(players, currentStichNumber, selectedGame);
            currentStichNumber++;
        }
        stich.addCard(players.get(currentPlayer),new Card(
                requestObject.getParams().get("wert").getAsString(),
                requestObject.getParams().get("farbe").getAsString()));
        send2All(requestObject);
        currentPlayer++;
        if(currentPlayer==spectator){
            currentPlayer++;
        }
        if (currentPlayer >= players.size()) {
            currentPlayer = 0;
        }
        if(currentPlayer==spectator){
            currentPlayer++;
        }

        if (stich.getCardMap().size() > 2) {
            try {
                int winner = stich.getWinner();

                stichList.add(stich);
                try{
                    points.put(winner, points.get(winner) + stich.calculatePoints());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                currentPlayer = winner;
                send2All(new UpdateUserPanel(players.stream().filter(p->p.getNumber()==winner)
                        .findFirst().get().getName()," hat Stich(e)"));
                if (currentStichNumber > 9) {
                    endIt();
                    return;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        if (currentStichNumber < 11) {
            send2All(new Wait4Player(players.stream().filter(p->p.getNumber()==currentPlayer).findFirst().get().getName()));
        }
    }

    @Override
    public void endIt() {
        super.endIt();
        SkatEndDialog e = new SkatEndDialog(selectedGame, players, stichList, getSkatPoints());
        send2All(new GameEnd(e.getReString1(), e.getKontraString1(),
                e.getPlayer1String(), e.getPlayer2String(), e.getPlayer3String(),
                e.getRemaining()));
        wait4NextRound = true;
        readyMap = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            readyMap.put(i, false);
        }
        if (c.skat.pflichtRamsch) {
            if (gamesTilRamsch == 0) {
                if(selectedGame== GameSelected.GAMES.Ramsch) {
                    gamesTilNormal--;
                    if (gamesTilNormal < 1) {
                        gamesTilRamsch = c.skat.wiederholRamsch;
                    }
                }
            } else {
                gamesTilRamsch--;
                if (gamesTilRamsch < 1) {
                    gamesTilNormal = players.size();
                }
            }
        }
    }

    private void runGame(int player){
        points = new HashMap<>();
        for (int i = 0;i< players.size();i++) {
            points.put(i,0);
        }
        currentPlayer = player;
        currentStichNumber =0;
        send2All(new Wait4Player(players.stream().filter(p -> p.getNumber()==player).findAny().get().getName()));
    }


    private int getSkatPoints() {
        int result = 0;
        for(BaseCard c : skat) {
            switch (c.value) {
                case Statics.ZEHN: {
                    result += 10;
                    break;
                }
                case Statics.BUBE: {
                    result += 2;
                    break;
                }
                case Statics.DAME: {
                    result += 3;
                    break;
                }
                case Statics.KOENIG: {
                    result += 4;
                    break;
                }
                case Statics.ASS: {
                    result += 11;
                    break;
                }
            }
        }
        return result;
    }

    private void handleGameSelected(RequestObject message) {
        String player = message.getParams().get("player").getAsString();
        selectedGame = GameSelected.GAMES.valueOf(message.getParams().get("game").getAsString());
        hand = message.getParams().get("hand").getAsBoolean();
        ouvert = message.getParams().get("ouvert").getAsBoolean();
        send2All(message);
        send2All(new DisplayMessage(player +" spielt " +selectedGame.name()
                + (hand ? " hand":"" )+ (ouvert ? " ouvert":"")));
        runGame(beginner);
        if(ouvert) {
            players.forEach(p -> {
                if (!p.getName().equals(player)) {
                    queueOut(p,new OuvertCards(player, players.stream().filter(q-> q.getName().equals(player))
                            .findFirst().get().getHand()));
                }
            });
        }
    }

    private void handleSchieben(RequestObject message) {
        send2All(new DisplayMessage(String.format("%s schiebt",message.getParams().get("player").getAsString())));
        askNextRamschPlayer();
    }

    private void handleSkat(RequestObject message) {
        JsonArray array = message.getParams().getAsJsonArray("cards");
        Player player = players.stream().filter(p->p.getName().equals(
                message.getParams().get("player").getAsString())).findFirst().get();
        skat = new ArrayList<>();
        array.forEach(card->{
            Card c = new Card(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            skat.add(c);
            player.getHand().removeIf(q-> q.value.equals(c.value) && q.farbe.equals(c.farbe));
        });
        if(selectedGame==GameSelected.GAMES.Ramsch){

            askNextRamschPlayer();
        }
    }



    private void askNextRamschPlayer() {
        if(nextRamschPlayer==hoeren){
            nextRamschPlayer= sagen;
            //send2All(new DisplayMessage(players.get(nextRamschPlayer).getName() +" bekommt den Skat"));
            queueOut(players.get(nextRamschPlayer),new RamschSkat());
        }
        else if(nextRamschPlayer == sagen){
            nextRamschPlayer= weitersagen;
            //send2All(new DisplayMessage(players.get(nextRamschPlayer).getName() +" bekommt den Skat"));
            queueOut(players.get(nextRamschPlayer),new RamschSkat());
        }
        else{
            runGame(beginner);
        }
    }

    private void askNextGrandHandPlayer() {
        if(nextRamschPlayer==hoeren){
            nextRamschPlayer= sagen;
            queueOut(players.get(nextRamschPlayer),new GrandHand(players.get(nextRamschPlayer).getName(),false));
        }
        else if(nextRamschPlayer == sagen){
            nextRamschPlayer= weitersagen;
            queueOut(players.get(nextRamschPlayer),new GrandHand(players.get(nextRamschPlayer).getName(),false));
        }
        else{
            selectedGame = GameSelected.GAMES.Ramsch;
            ramsch();
        }
    }

    private void handleGetSkat(RequestObject message) {
        queueOut(players.stream().filter(player -> player.getName().equals(message.getParams().get("player").getAsString())).findFirst().get(),
                new Skat("server",skat));
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
                    if (currentGameValue>0) {
                        selectGame(hoeren);
                    }
                    else{
                        askNext(hoeren);
                    }
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
        setPlayerRoles();
        nextRamschPlayer = hoeren;
        send2All(new DisplayMessage("Ramsch: " + players.get(beginner).getName() + " bekommt den Skat"));
        queueOut(players.get(beginner),new RamschSkat());
     }

    private void selectGame(int playerNumber) {
        players.get(playerNumber).setRe(true);
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
                        String.format("%s: %s",
                                message.getParams().get("player").getAsString(),
                                currentGameValue)));
                if (p == hoeren) {
                    selectGame(hoeren);
                } else if (p == sagen) {
                    queueOut(players.get(hoeren), new Reizen(players.get(hoeren).getName(), currentGameValue, false));
                } else if (p == weitersagen) {
                    if (sagen < 0) {
                        queueOut(players.get(hoeren), new Reizen(players.get(hoeren).getName(), currentGameValue, false));
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

    @Override
    public void shuffleCards() {
        for (Player player1 : players) {
            send2All(new UpdateUserPanel(player1.getName(), ""));
        }
        try {
            stichList = new ArrayList<>();
            random = new Random(System.currentTimeMillis());
            List<Card> cardList = Card.createCardList();

            players.forEach(player -> {
                player.setHand(new ArrayList<>());
                player.setPoints(0);
                if (!player.isSpectator()) {
                    for (int i = 0; i < 10; i++) {
                        Card card = cardList.get(random.nextInt(cardList.size()));
                        player.getHand().add(card);
                        cardList.remove(card);
                    }
                }
            });
            skat = new ArrayList<>();
            for(int i=0;i<2;i++) {
                skat.add(cardList.get(i));
            }
            players.forEach(player -> queueOut(player,new Cards(player.getHand())));


            //TODO: Extract method to reset all game variables for a new game

            hand= false;
            ouvert = false;
            currentGameValue = 0;
            selectedGame = GameSelected.GAMES.Ramsch;
            points = new HashMap<>();
            setPlayerRoles();

            if(gamesTilNormal<1){
                queueOut(players.get(sagen),new Reizen(players.get(sagen).getName(),currentGameValue,true));
            }
            else if(gamesTilRamsch<1){
                nextRamschPlayer = hoeren;
                queueOut(players.get(hoeren),new GrandHand(players.get(hoeren).getName(),false));
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void startGame() {
        super.startGame();
        send2All(new AnnounceSpectator(spectator,beginner));
        if(c.skat.pflichtRamsch){
            gamesTilRamsch = c.skat.beginnRamsch *players.size();
            if(gamesTilRamsch==0){
                gamesTilNormal = players.size();
            }
            else{
                gamesTilNormal = 0;
            }
        }
        shuffleCards();
    }

    private void nextGame() {
        beginner++;
        if (beginner > players.size() - 1) {
            beginner = 0;
        }
        if (players.size() > 3) {
            spectator = beginner - 1;
            if (spectator < 0) {
                spectator = players.size() - 1;
            }
        }
        players.forEach(player -> {
            player.setSpectator(false);
            player.setRe(false);
        });
        if (players.size() > 3) {
            players.get(spectator).setSpectator(true);
        }
        send2All(new AnnounceSpectator(spectator, beginner));
        shuffleCards();
    }


    private void setPlayerRoles(){
        hoeren = beginner;
        sagen = nextNotSpectator(hoeren);
        weitersagen = nextNotSpectator(sagen);
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
