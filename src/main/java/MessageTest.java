import base.BaseCard;
import base.MessageIn;
import base.Player;
import base.Statics;
import base.doko.Card;
import base.doko.messages.GameEnd;
import base.doko.messages.SelectCards4Armut;
import base.doko.messages.SelectGame;
import base.doko.messages.SendCards;
import base.messages.*;
import base.messages.admin.AbortGame;
import base.messages.admin.SetAdmin;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MessageTest implements IServerMessageHandler{

    private List<Socket> socketList;

    public static void main(String[] args) {
        new MessageTest();
    }

    public MessageTest() {

        Random r = new Random(System.currentTimeMillis());
        this.socketList = new ArrayList<>();
        ComServer server = new ComServer(5000);
        server.setServer(this);

        JFrame testFrame = new JFrame("MessageTest");
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));
        JPanel basePanel = new JPanel(new GridLayout(18, 1));
        JPanel dokoPanel = new JPanel(new GridLayout(10, 1));

        JButton abortGame = new JButton("AbortGame");
        abortGame.addActionListener(e -> server.send2All(socketList, new AbortGame()));
        basePanel.add(abortGame);
        JButton setAdmin = new JButton("SetAdmin");
        setAdmin.addActionListener(e -> server.send2All(socketList, new SetAdmin(true)));
        basePanel.add(setAdmin);
        JButton announceSpectator = new JButton("AnnounceSpectator");
        announceSpectator.addActionListener(e -> server.send2All(socketList, new AnnounceSpectator(0, 0)));
        basePanel.add(announceSpectator);
        JButton cards = new JButton("Cards");

        cards.addActionListener(e ->{
            List<BaseCard> baseCardList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                baseCardList.add(Card.randomCard(r));
            }
            server.send2All(socketList, new Cards(baseCardList));
        });
        basePanel.add(cards);
        JButton currentStich = new JButton("currentStich");
        currentStich.addActionListener(e -> server.send2All(socketList, new CurrentStich()));
        basePanel.add(currentStich);
        JButton displayMessage = new JButton("displayMessage");
        displayMessage.addActionListener(e -> server.send2All(socketList, new DisplayMessage("" + System.currentTimeMillis())));
        basePanel.add(displayMessage);
        JButton startGame = new JButton("startGame");
        startGame.addActionListener(e -> server.send2All(socketList, new StartGame(Statics.game.DOKO.name())));
        basePanel.add(startGame);
        JButton playersInLobby = new JButton("PlayersInLobby");
        playersInLobby.addActionListener(e ->{
            List<String> players = Arrays.asList("Android","test1","test2","test3");
            server.send2All(socketList, new PlayersInLobby(players));
        });
        basePanel.add(playersInLobby);

        JButton resetSockets = new JButton("reset");
        resetSockets.addActionListener(e -> socketList = new ArrayList<>());
        basePanel.add(resetSockets);


        mainPanel.add(basePanel);


        JButton gameEnd = new JButton("gameEnd");
        gameEnd.addActionListener(e -> server.send2All(socketList, new GameEnd(
                "Test<br>Test1<br>Test2",
                "1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>0",
                "Test<br>Test1<br>Test2",
                "1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>0",
                0)));
        dokoPanel.add(gameEnd);
        JButton sendCards = new JButton("SendCards");
        sendCards.addActionListener(e -> {
            List<BaseCard> cardList = new ArrayList<>();
            for(int i = 0 ; i<3; i++) {
                cardList.add(Card.randomCard(r));
            }
            server.send2All(socketList, new SendCards(cardList, SendCards.RICH));
        });
        dokoPanel.add(sendCards);
        JButton selectCards4Armut = new JButton("selectCards4Armut");
        selectCards4Armut.addActionListener(e -> server.send2All(socketList,new SelectCards4Armut()));
        dokoPanel.add(selectCards4Armut);
        JButton selectGame = new JButton("selectGame");
        selectGame.addActionListener(e -> server.send2All(socketList,new SelectGame()));
        dokoPanel.add(selectGame);

        mainPanel.add(dokoPanel);

        testFrame.add(mainPanel);
        testFrame.pack();
        testFrame.setVisible(true);

    }

    @Override
    public void handleInput(MessageIn message) {
        if(!socketList.contains(message.getSocket())){
            socketList.add(message.getSocket());
        }
    }
}
