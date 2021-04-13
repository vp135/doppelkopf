import base.BaseCard;
import base.messages.*;
import base.skat.Card;
import base.skat.SortHand;
import base.skat.messages.GameSelected;
import base.skat.messages.Reizen;
import com.google.gson.JsonArray;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SkatClient extends BaseClient implements IInputputHandler{


    private JButton sortKaro;
    private JButton sortHerz;
    private JButton sortPik;
    private JButton sortKreuz;
    private JButton sortNull;
    private JButton sortGrand;
    private GameSelected.GAMES selectedGame;
    private int spectator;
    private int currentCardsOnTable;
    private int aufspieler;
    private JButton nextValue;
    private JButton pass;

    public SkatClient(ComClient handler, List<String> players, Configuration c) {
        super(handler, players, c);
    }

    @Override
    protected void createGameSpecificButtons() {
        sortKaro = new JButton("Karo");
        sortHerz = new JButton("Herz");
        sortPik = new JButton("Pik");
        sortKreuz = new JButton("Kreuz");
        sortNull = new JButton("Null");
        sortGrand = new JButton("Grand");
        nextValue = new JButton("18");
        pass = new JButton("weg!");
        buttonList = new ArrayList<>();
        buttonList.add(sortKaro);
        buttonList.add(sortHerz);
        buttonList.add(sortPik);
        buttonList.add(sortKreuz);
        buttonList.add(sortNull);
        buttonList.add(sortGrand);
        buttonList.add(nextValue);
        buttonList.add(pass);

        sortKaro.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortKaro(hand));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
        });
        sortNull.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortNull(hand));
            deselectAllSortButtons();
            sortNull.setBackground(Color.GREEN);
        });
        sortGrand.addActionListener(e -> {
            createCardButtons(hand= SortHand.sortGrand(hand));
            deselectAllSortButtons();
            sortGrand.setBackground(Color.GREEN);
        });
        nextValue.addActionListener(e->{
            handler.queueOutMessage(new Reizen(c.name,Integer.parseInt(nextValue.getText()),true));
        });
    }

    @Override
    protected void setGameSpecificButtons(List<BaseCard> hand) {

        controlPanel.removeAll();
        buttonList.forEach(button-> controlPanel.add(button));
        Dimension d = new Dimension(mainFrame.getSize().width,mainFrame.getSize().height/(30));
        setComponentSizes(controlPanel,d);
    }

    @Override
    protected void setGameSpecifics() {
        maxHandCards = 12;
    }

    @Override
    protected void setCardClickAdapter() {
        cardClickAdapter = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel)e.getSource();
                BaseCard card = cardMap.get(label);
                if(wait4Player){
                    wait4Player = false;
                    hand.remove(card);
                    label.setVisible(false);
                    handler.queueOutMessage(new PutCard(players.indexOf(c.name),card.farbe,card.value));
                    serverMessageLabel.setText("");
                    if(c.redrawCards){
                        createCardButtons(hand);
                    }
                }
            }
        };
    }

    @Override
    public void handleInput(RequestObject message) {
        super.handleInput(message);
        switch (message.getCommand()){
            case Cards.COMMAND:
                deselectAllSortButtons();
                handleCards(message);
                break;
            case PutCard.COMMAND:
                handlePutCard(message);
                break;
            case CurrentStich.LAST:
            case CurrentStich.SPECIFIC:
                if(letzterStich==null){
                    handleLastStich(message);
                }
                break;
            case Wait4Player.COMMAND:
                handleWait4Player(message);
                break;
            case GameType.COMMAND:
                handleGameType(message);
                break;
            case GameEnd.COMMAND:
                handleGameEnd(message);
                break;
            case UpdateUserPanel.COMMAND:
                handleUserPanelUpdate(message);
            case AnnounceSpectator.COMMAND:
                handleAnnounceSpectator(message);
                break;
            case Reizen.COMMAND:{
                handleReizen(message);
            }
            default:
                break;

        }
    }

    private void handleReizen(RequestObject message) {
        int val = message.getParams().get("value").getAsInt();
        int nextVal = 0;
        for(int i =0; i<Reizen.VALUES.length;i++){
            if(Reizen.VALUES[i]==val){
                nextVal = Reizen.VALUES[i+1];
                break;
            }
        }
        nextValue.setText(String.valueOf(nextVal));
        gameMessageLabel.setText(String.valueOf(message.getParams().get("value").getAsInt()));
    }

    private void handleAnnounceSpectator(RequestObject message) {
        spectator = message.getParams().get("player").getAsInt();
        aufspieler = message.getParams().get("starter").getAsInt();
        if(players.size()>3 && players.get(spectator).equals(c.name)) {
            handler.queueOutMessage(new DisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    private void handleUserPanelUpdate(RequestObject message) {
        int ownNumber = players.indexOf(c.name);
        List<Integer> tmpList= new ArrayList<>();
        int i = ownNumber;
        while (tmpList.size()<4){
            if(i!=spectator){
                tmpList.add(i);
            }
            i++;
            if(i>players.size()-1){
                i = 0;
            }
        }
        int otherNumber = players.indexOf(message.getParams().get("player").getAsString());

        switch (tmpList.indexOf(otherNumber)){
            case 1:{
                userLabel_1.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 2:{
                userLabel_2.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 3:{
                userLabel_3.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 0:{
                if(message.getParams().get("player").getAsString().equals(c.name)){
                    userLabel_4.setText(createUserLabelString(
                            message.getParams().get("text").getAsString(),
                            "Du",
                            false));
                }
                else{
                    userLabel_4.setText(createUserLabelString(
                            message.getParams().get("text").getAsString(),
                            message.getParams().get("player").getAsString(),
                            true));
                }
                break;
            }
        }
        updateTable();
    }


    private void handleGameEnd(RequestObject message) {
        updateTable();
        EndDialog e = new EndDialog(
                message.getParams().get("re1").getAsString(),
                message.getParams().get("re2").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("kontra2").getAsString(),
                message.getParams().get("remain").getAsInt());
        e.showDialog(this.mainFrame);
        clearPlayArea();
        selectCards = false;
        wait4Player = false;
        hand = new ArrayList<>();
        handler.queueOutMessage(new ReadyForNextRound(players.indexOf(c.name)));
    }

    private void handleGameType(RequestObject message) {
        selectedGame = GameSelected.GAMES.valueOf(
                message.getParams().get(GameType.COMMAND).getAsString());
    }

    @Override
    protected void handleWait4Player(RequestObject message) {
        super.handleWait4Player(message);
    }

    private void handleLastStich(RequestObject message) {
        JLabel cardPos1 = new JLabel();
        JLabel cardPos2 = new JLabel();
        JLabel cardPos3 = new JLabel();
        JLabel cardPos4 = new JLabel();
        int ownNumber = players.indexOf(c.name);
        List<Integer> tmpList= new ArrayList<>();
        int i = ownNumber;
        while (tmpList.size()<3){
            if(i!=spectator){
                tmpList.add(i);
            }
            i++;
            if(i>players.size()-1){
                i = 0;
            }
        }

        for(int j = 0;j<tmpList.size();j++){
            if (message.getParams().has(String.valueOf(tmpList.get(j)))) {
                if (j == 0) {
                    cardPos4 = getCardLabel(new base.doko.Card(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 1) {
                    cardPos1 = getCardLabel(new base.doko.Card(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 2) {
                    cardPos2 = getCardLabel(new base.doko.Card(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 3) {
                    cardPos3 = getCardLabel(new base.doko.Card(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                }
            }
        }

        letzterStich = new JFrame("letzter Stich");
        JPanel jPanel = new JPanel(new GridLayout(3,3));
        jPanel.add(new JLabel());
        jPanel.add(cardPos2);
        jPanel.add(new JLabel());
        jPanel.add(cardPos1);
        jPanel.add(new JLabel());
        jPanel.add(cardPos3);
        jPanel.add(new JLabel());
        jPanel.add(cardPos4);
        jPanel.add(new JLabel());
        letzterStich.add(jPanel);
        letzterStich.pack();
        letzterStich.setVisible(true);
    }

    private void handlePutCard(RequestObject message) {
        int ownNumber = players.indexOf(c.name);
        List<Integer> tmpList = new ArrayList<>();
        int i = ownNumber;

        while (tmpList.size()<3){
            if(i!=spectator){
                tmpList.add(i);
            }
            i++;
            if(i>players.size()-1){
                i=0;
            }
        }

        if(currentCardsOnTable>2){
            clearPlayArea();
            if(letzterStich!=null){
                letzterStich.dispose();
            }
            updateTable();
            currentCardsOnTable = 0;
            tableStich.clear();
        }

        int player = message.getParams().get("player").getAsInt();
        Card card = new Card(
                message.getParams().get("wert").getAsString(),
                message.getParams().get("farbe").getAsString());

        for(int j:tmpList){
            if(player==j){
                drawCard2Position(card,tmpList.indexOf(j),table.getHeight(),table.getWidth());
                tableStich.put(tmpList.indexOf(j),card);
                break;
            }
        }
        currentCardsOnTable++;
        updateTable();
    }

    @Override
    protected void handleCards(RequestObject message) {
        selectedGame = GameSelected.GAMES.UNDEFINED;
        super.handleCards(message);

    }

    private String createUserLabelString(String msg, String player, boolean append2Name) {
        StringBuilder s = new StringBuilder();
        String color = "white";
        try {
            if (aufspieler > -1 && player.equals(players.get(aufspieler))) {
                color = "red";
            }
        }catch (Exception ex){
            log.error("Aufspieler: " +aufspieler);
        }
        if (append2Name) {
            s.append(player);
        }
        if(msg.length()>0) {
            s.append("<br>hat Stich(e)");
        }
        return addColor(s.toString(),color);
    }

    private static String addColor(String s, String color){
        return "<html><font color=\"" +
                color +
                "\">" +
                s +
                "</font></html>";
    }

    @Override
    protected void createUI(int state, int posX, int posY, Dimension size, boolean test) {
        super.createUI(state, posX, posY, size, test);
        setGameSpecificButtons(null);
        controlPanel.setVisible(true);
    }
}
