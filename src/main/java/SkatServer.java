import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.*;
import base.skat.Card;
import base.skat.messages.GameSelected;
import base.skat.messages.Passen;
import base.skat.messages.Reizen;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkatServer extends BaseServer{


    private int beginner =0;
    private int hoeren =0;
    private int sagen = 0;
    private int weitersagen =0;
    private int currentPlayer = 0;
    private GameSelected.GAMES selectedGame = GameSelected.GAMES.UNDEFINED;

    private int spectator=3;

    private int currentGameValue = 0;
    private Random random;

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
        }
    }

    private void handlePassen(RequestObject message) {
        send2All(new DisplayMessage(message.getParams().get("player").getAsString() + ": weg"));
    }

    private void handleReizen(RequestObject message) {

        if(message.getParams().get("active").getAsBoolean()) {
            currentGameValue = message.getParams().get("value").getAsInt();
            send2All(new DisplayMessage(
                    String.format("%s sagt %s",
                            message.getParams().get("player").getAsString(),
                            currentGameValue)));
            queueOut(players.get(hoeren), new Reizen(players.get(hoeren).getName(), currentGameValue, false));
        }
        else {
            send2All(new DisplayMessage(
                    String.format("%s sagt %s",
                            message.getParams().get("player").getAsString(),
                            "Ja")));
            queueOut(players.get(sagen), new Reizen(players.get(sagen).getName(), currentGameValue, true));
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

            players.forEach(player -> queueOut(player,new Cards(player.getHand())));


            //TODO: Extract method to reset all game variables for a new game
            //wait4Gesund = true;
            //armutplayer =-1;
            //schwein = false;
            //gameSelection = new HashMap<>();
            //send2All(new SelectGame());
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
