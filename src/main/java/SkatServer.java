import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.PutCard;
import base.messages.RequestObject;
import base.skat.messages.Reizen;
import base.skat.messages.GameSelected;

import java.net.Socket;

public class SkatServer extends BaseServer{


    private int beginner =0;
    private int currentPlayer = 0;
    private GameSelected.GAMES selectedGame = GameSelected.GAMES.UNDEFINED;

    private int aufspieler = -1;
    private int armutplayer = -1;
    private int hochzeitSpieler = -1;
    private int spectator=4;

    private int currentGameValue = 0;

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

        }
    }

    private void handleReizen(RequestObject message) {
        Reizen m = (Reizen) message;

    }

    private void reizen(Player player, boolean active){
        queueOut(player,new Reizen(player,currentGameValue,active));
    }

}
