import base.Card;
import base.messages.Message;
import base.messages.MessageAbortGame;
import base.messages.MessageAddPlayer;
import base.messages.MessageCards;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        MessageAbortGame messageAbortGame = new MessageAbortGame("Server", "test", "playerName");
        MessageAddPlayer messageAddPlayer = new MessageAddPlayer("Server", "test", "playerName");
        List<Card> cards = DokoServer.createCardList();
        MessageCards messageCards = new MessageCards("Server", "test", cards);

        System.out.println(messageAbortGame.toJson());
        System.out.println(messageAddPlayer.toJson());
        System.out.println(messageCards.toJson());

    }
}
