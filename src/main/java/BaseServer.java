import base.Logger;
import base.MessageIn;
import base.Player;
import base.Statics;
import base.messages.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseServer implements IServerMessageHandler{

    protected Logger log = new Logger(this.getClass().getName(),1);
    protected Statics.game gameType;
    protected ComServer comServer;
    protected final List<Player> players = new ArrayList<>();
    protected Configuration c;

    public BaseServer(Configuration c, ComServer comServer) {
        this.c = c;
        this.comServer = comServer;
        comServer.setServer(this);
    }


    protected void startGame() {
        send2All(new StartGame(gameType.name()));
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void queueOut(Player player, RequestObject message) {
        comServer.queueOut(player.getSocket(),message,true);
    }

    protected void send2All(RequestObject message){
        List<Socket> socketList = new ArrayList<>();
        players.forEach(player -> socketList.add(player.getSocket()));
        comServer.send2All(socketList, message);
    }

    @Override
    public void handleInput(MessageIn message) {
        RequestObject requestObject = RequestObject.fromString(message.getInput());
        Socket socket = message.getSocket();
        switch (requestObject.getCommand()) {
            case AddPlayer.COMMAND:
                String name = requestObject.getParams().get("player").getAsString();
                if(players.stream().noneMatch(player -> player.getName().equals(name))) {
                    players.add(new Player(requestObject.getParams().get("player").getAsString(),
                            players.size(), socket, false));

                    List<String> list = new ArrayList<>();
                    players.forEach(p -> list.add(p.getName()));
                    send2All(new PlayersInLobby(list));
                }
                else{
                    players.stream().filter(player -> player.getName().equals(name)).findFirst().ifPresent(player -> {
                        player.setSocket(socket);
                        List<String> list = new ArrayList<>();
                        players.forEach(p -> list.add(p.getName()));
                        comServer.queueOut(player.getSocket(), new PlayersInLobby(list),true);
                    });
                }
                break;
            case GetVersion.COMMAND:
                comServer.queueOut(socket,
                        new GetVersion("Server", Statics.VERSION),true);
                break;

            default:
                log.error("message type unknown. Message not processed");
                break;
        }
    }

}
