import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.*;
import base.skat.Card;
import base.skat.messages.GameSelected;
import base.skat.messages.Reizen;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SkatServer extends BaseServer{


    private int beginner =0;
    private int currentPlayer = 0;
    private GameSelected.GAMES selectedGame = GameSelected.GAMES.UNDEFINED;

    private int aufspieler = -1;
    private int armutplayer = -1;
    private int hochzeitSpieler = -1;
    private int spectator=4;

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
        }
    }

    private void handleReizen(RequestObject message) {
        if(message.getParams().get("active").getAsBoolean()){
            currentGameValue = message.getParams().get("value").getAsInt();
            send2All(new Reizen(players.get(0).getName(),currentGameValue,false));
        }
        /*send2All(new DisplayMessage(
                String.format("%s sagt %s",
                        message.getParams().get("player").getAsString(),
                        message.getParams().get("value").getAsInt())));

         */
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


            Cards cards = new Cards(players.get(0).getHand());
            send2All(cards);


            //TODO: Extract method to reset all game variables for a new game
            //wait4Gesund = true;
            //armutplayer =-1;
            //schwein = false;
            //gameSelection = new HashMap<>();
            //send2All(new SelectGame());
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

}
