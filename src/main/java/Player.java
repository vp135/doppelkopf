import base.Card;

import java.net.Socket;
import java.util.List;

public class Player {
    private String name;
    private int number;
    private Socket socket;
    private boolean spectator;
    private List<Card> hand;


    public Player(String name, int number, Socket socket, boolean spectator) {
        this.name = name;
        this.number = number;
        this.socket = socket;
        this.spectator = spectator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }
}
