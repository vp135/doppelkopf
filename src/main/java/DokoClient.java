import base.BaseCard;
import base.Statics;
import base.doko.Card;
import base.doko.messages.GameEnd;
import base.doko.SortHand;
import base.doko.messages.*;
import base.messages.*;
import com.google.gson.JsonArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DokoClient extends BaseClient implements  IInputputHandler{

    //UI
    private JButton sortNormal;
    private JButton sortHochzeit;
    private JButton sortBuben;
    private JButton sortDamen;
    private JButton sortBubenDamen;
    private JButton sortFleischlos;
    private JButton sortKreuz;
    private JButton sortPik;
    private JButton sortHerz;
    private JButton sortKaro;
    private JButton sortArmut;
    private JButton sortKoenige;


    //Spielvariablen

    private boolean schweinExists = false;
    private String selectedGame = GameSelected.NORMAL;
    private int spectator;
    private int aufspieler;
    private int currentCardsOnTable = 0;



    //Configuration

    //private final ServerConfig serverConfig = new ServerConfig();


    public DokoClient(ComClient handler, List<String> players, Configuration c){
        super(handler,players,c);
    }

    @Override
    public void createGameSpecificButtons(){
        sortNormal = new JButton("Gesund");
        sortBuben = new JButton("Buben");
        sortDamen = new JButton("Damen");
        sortBubenDamen = new JButton("Buben-Damen");
        sortFleischlos = new JButton("Fleischlos");
        sortKreuz = new JButton("Kreuz");
        sortPik = new JButton("Pik");
        sortHerz = new JButton("Herz");
        sortKaro = new JButton("Karo");
        sortArmut = new JButton("Armut");
        sortHochzeit = new JButton("Hochzeit");
        sortKoenige = new JButton(">4 Koenige");
        buttonList = new ArrayList<>();
        buttonList.add(sortNormal);
        buttonList.add(sortBuben);
        buttonList.add(sortDamen);
        buttonList.add(sortBubenDamen);
        buttonList.add(sortFleischlos);
        buttonList.add(sortKreuz);
        buttonList.add(sortPik);
        buttonList.add(sortHerz);
        buttonList.add(sortKaro);
        buttonList.add(sortArmut);
        buttonList.add(sortHochzeit);
        buttonList.add(sortKoenige);


        sortNormal.addActionListener(e->{
            createCardButtons(hand= SortHand.sortNormal(hand, schweinExists));
            deselectAllSortButtons();
            sortNormal.setBackground(Color.GREEN);
            selectedGame = GameSelected.NORMAL;
        });
        sortDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortDamenSolo(hand));
            deselectAllSortButtons();
            sortDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.DAMEN;
        });
        sortBuben.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenSolo(hand));
            deselectAllSortButtons();
            sortBuben.setBackground(Color.GREEN);
            selectedGame = GameSelected.BUBEN;
        });
        sortBubenDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenDamenSolo(hand));
            deselectAllSortButtons();
            sortBubenDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.BUBENDAMEN;
        });
        sortFleischlos.addActionListener(e->{
            createCardButtons(hand=SortHand.sortFleischlos(hand));
            deselectAllSortButtons();
            sortFleischlos.setBackground(Color.GREEN);
            selectedGame = GameSelected.FLEISCHLOS;
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
            selectedGame = GameSelected.KREUZ;
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
            selectedGame = GameSelected.PIK;
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
            selectedGame = GameSelected.HERZ;
        });
        sortKaro.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
            selectedGame = GameSelected.KARO;
        });
        sortArmut.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortArmut(hand, schweinExists));
            deselectAllSortButtons();
            sortArmut.setBackground(Color.GREEN);
            selectedGame = GameSelected.ARMUT;
        });
        sortKoenige.addActionListener(e->{
            createCardButtons(hand=SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKoenige.setBackground(Color.GREEN);
            selectedGame = GameSelected.KOENIGE;
        });
        sortHochzeit.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortNormal(hand,schweinExists));
            deselectAllSortButtons();
            sortHochzeit.setBackground(Color.GREEN);
            selectedGame = GameSelected.HOCHZEIT;
        });
    }

    @Override
    protected void setGameSpecificButtons(List<BaseCard> cards){

        controlPanel.removeAll();
        JButton vorbehalt = new JButton("OK");
        vorbehalt.addActionListener(e ->{
            handler.queueOutMessage(new GameSelected(players.indexOf(c.connection.name),selectedGame));
            controlPanel.setVisible(false);
            controlPanel.removeAll();
        });

        controlPanel.add(sortNormal);
        controlPanel.add(sortDamen);
        controlPanel.add(sortBuben);
        controlPanel.add(sortBubenDamen);
        controlPanel.add(sortFleischlos);
        controlPanel.add(sortKreuz);
        controlPanel.add(sortPik);
        controlPanel.add(sortHerz);
        controlPanel.add(sortKaro);

        if(cards.stream().filter(p->p.trumpf).count()<4){
            controlPanel.add(sortArmut);
        }
        if(cards.stream().filter(p->p.value.equals(Statics.KOENIG)).count()>4) {
            controlPanel.add(sortKoenige);
        }
        if(cards.stream().filter(p->p.value.equals(Statics.DAME)&&p.farbe.equals(Statics.KREUZ)).count()>1){
            controlPanel.add(sortHochzeit);
        }

        controlPanel.add(vorbehalt);
        Dimension d = new Dimension(mainFrame.getSize().width,mainFrame.getSize().height/(30));
        setComponentSizes(controlPanel,d);
        controlPanel.setVisible(true);
    }

    @Override
    public void setGameSpecifics() {
        maxHandCards = 12;
        heightCorrection = 1f;
    }

    @Override
    protected void redrawEverything() {
        super.redrawEverything();
        if (hand != null) {
            createCardButtons(hand);
            clearPlayArea();
            tableStich.keySet().forEach(i ->
                    drawCard2Position(tableStich.get(i), i, table.getHeight(), table.getWidth()));
        }
    }

    @Override
    protected void setCardClickAdapter() {
        handCardClickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                BaseCard card = cardMap.get(label);
                if (selectCards) {
                    moveCard2Exchange(card);
                    if(hand.size()==10){
                        setSendCardButton(SendCards.POOR, "Karten zurückgeben");
                    }
                    else{
                        controlPanel.removeAll();
                    }
                } else {
                    if (wait4Player || test) {
                        wait4Player = false;
                        hand.remove(card);
                        label.setVisible(false);
                        handler.queueOutMessage(new PutCard(players.indexOf(c.connection.name), card.farbe, card.value));
                        //serverMessageLabel.setText("");
                        if (c.ui.redrawCards) {
                            createCardButtons(hand);
                        }
                    }
                }
            }
        };
        exchangeCardClickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                BaseCard card = null;
                for(int i = 0; i<exchangeCards.length;i++){
                    if(cLabels[i]!=null && cLabels[i]==label){
                        card = exchangeCards[i];
                        break;
                    }
                }
                moveCard2Hand(card);
                if(hand.size()==10){
                    setSendCardButton(SendCards.POOR, "Karten zurückgeben");
                }
                else{
                    controlPanel.removeAll();
                }
                for(int i = 0;i<exchangeCards.length;i++){
                    if(exchangeCards[i]==card){
                        exchangeCards[i] = null;
                        cLabels[i].setVisible(false);
                        break;
                    }
                }
                middlePanel.revalidate();
                middlePanel.repaint();
            }
        };
    }



    @Override
    public void handleInput(RequestObject message) {
        super.handleInput(message);
        switch (message.getCommand()) {
            case Cards.COMMAND: {
                deselectAllSortButtons();
                handleCards(message);
                break;
            }
            case PutCard.COMMAND: {
                handlePutCard(message);
                break;
            }
            case CurrentStich.LAST: { }
            case CurrentStich.SPECIFIC: {
                if(letzterStich==null) {
                    handleLastStich(message);
                }
                break;
            }
            case Wait4Player.COMMAND: {
                handleWait4Player(message);
                break;
            }
            case GameEnd.COMMAND: {
                handleGameEnd(message);
                break;
            }
            case SelectGame.COMMAND: {
                controlPanel.setVisible(players.indexOf(c.connection.name) != spectator);
                break;
            }
            case GameType.COMMAND: {
                handleGameType(message);
                break;
            }
            case SelectCards4Armut.COMMAND: {
                selectCards4Armut();
                break;
            }
            case SendCards.COMMAND: {
                handleSendCards(message);
                break;
            }
            case GetArmut.COMMAND: {
                handleGetArmut();
                break;
            }

            case UpdateUserPanel.COMMAND: {
                handleUserPanelUpdate(message);
                break;
            }
            case AnnounceSpectator.COMMAND: {
                handleAnnounceSpectator(message);
                break;
            }
            default:
                break;
        }
    }

    // handle messages

    private void handlePutCard(RequestObject object){
        int ownNumber = players.indexOf(c.connection.name);
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

        if(currentCardsOnTable>3){
            clearPlayArea();
            if(letzterStich!=null) {
                letzterStich.dispose();
                letzterStich=null;
            }
            updateTable();
            currentCardsOnTable = 0;
            tableStich.clear();
        }

        int player = object.getParams().get("player").getAsInt();
        Card card = new Card(
                object.getParams().get("wert").getAsString(),
                object.getParams().get("farbe").getAsString());

        for(int j :tmpList){
            if(player==j){
                drawCard2Position(card, tmpList.indexOf(j), table.getHeight(), table.getWidth());
                tableStich.put(tmpList.indexOf(j),card);
                break;
            }
        }
        currentCardsOnTable++;
        updateTable();
    }

    private void handleAnnounceSpectator(RequestObject message) {
        spectator = message.getParams().get("player").getAsInt();
        aufspieler = message.getParams().get("starter").getAsInt();
        if(players.size()>4 && players.get(spectator).equals(c.connection.name)) {
            handler.queueOutMessage(new DisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    @Override
    protected void handleWait4Player(RequestObject message) {
        super.handleWait4Player(message);
    }

    private void handleGetArmut() {
        if (JOptionPane.showConfirmDialog(mainFrame, "Armut mitnehmen?",
                "Armut mitnehmen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            handler.queueOutMessage(new GetArmut(players.indexOf(c.connection.name), true));
        } else {
            handler.queueOutMessage(new GetArmut(players.indexOf(c.connection.name), false));
        }
    }

    private void handleSendCards(RequestObject message) {
        boolean isRich = (message.getParams().get("receiver").getAsString().equals(SendCards.RICH));
        middlePanel.removeAll();
        selectCards = isRich;
        JsonArray array = message.getParams().getAsJsonArray("cards");
        exchangeCards = new Card[array.size()];
        for (int i = 0; i< exchangeCards.length;i++){
            exchangeCards[i]= new Card(array.get(i).getAsString().split(" ")[1], array.get(i).getAsString().split(" ")[0]);
        }



        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0;i<exchangeCards.length;i++){
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            if(isRich) {
                cLabels[i].addMouseListener(exchangeCardClickAdapter);
            }
            middlePanel.add(cLabels[i]);
        }

        if(isRich){
            setSendCardButton(SendCards.POOR, "Karten zurückgeben");
        }
        else {
            setAcceptArmutReturn();
        }

        middlePanel.setVisible(true);
        middlePanel.revalidate();
        middlePanel.repaint();

    }

    private void setAcceptArmutReturn() {
        controlPanel.removeAll();
        JButton button = new JButton("Karten aufnehmen");
        button.addActionListener(e -> {
            for (BaseCard exchangeCard : exchangeCards) {
                moveCard2Hand(exchangeCard);
            }
            handler.queueOutMessage(new CardsReturned());
            middlePanel.removeAll();
            middlePanel.setVisible(false);
            controlPanel.removeAll();
            controlPanel.setVisible(false);
            clearPlayArea();
        });
        controlPanel.add(button);
        controlPanel.setVisible(true);
    }

    private void handleGameType(RequestObject message) {
        selectedGame = message.getParams().get(GameType.COMMAND).getAsString();
        if (hand != null && hand.size() > 0) {
            hand = SortHand.sort(hand,selectedGame,schweinExists);
            createCardButtons(hand);
            if (selectedGame.equals(GameSelected.NORMAL)
                    || selectedGame.equals(GameSelected.KARO)
                    || selectedGame.equals(GameSelected.ARMUT)) {
                if (hand.stream().filter(p -> p.farbe.equals(Statics.KARO)
                        && p.value.equals(Statics.ASS)).count() > 1) {
                    handler.queueOutMessage(new SchweinExists());
                    schweinExists = true;
                } else {
                    schweinExists = false;
                }
            }
        }
        aufspieler = -1;
    }

    private void handleGameEnd(RequestObject message) {
        updateTable();
        DokoEndDialog e = new DokoEndDialog(
                message.getParams().get("re1").getAsString(),
                message.getParams().get("re2").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("kontra2").getAsString(),
                message.getParams().get("remain").getAsInt());
        e.showDialog(this.mainFrame);
        clearPlayArea();
        schweinExists = false;
        selectCards = false;
        wait4Player = false;
        hand = new ArrayList<>();
        serverMessages = new ArrayList<>();
        handler.queueOutMessage(new ReadyForNextRound(players.indexOf(c.connection.name)));
    }

    @Override
    protected void handleCards(RequestObject message) {
        selectedGame = GameSelected.NORMAL;
        JsonArray array = message.getParams().getAsJsonArray("cards");
        hand = new ArrayList<>();
        array.forEach(card->{
            Card c = new Card(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            hand.add(c);
        });
        super.handleCards(message);
    }

    private void handleLastStich(RequestObject stich) {
        JLabel cardPos1 = new JLabel();
        JLabel cardPos2 = new JLabel();
        JLabel cardPos3 = new JLabel();
        JLabel cardPos4 = new JLabel();
        int ownNumber = players.indexOf(c.connection.name);
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

        for(int j = 0;j<tmpList.size();j++){
            if (stich.getParams().has(String.valueOf(tmpList.get(j)))) {
                if (j == 0) {
                    cardPos4 = getCardLabel(new Card(
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 1) {
                    cardPos1 = getCardLabel(new Card(
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 2) {
                    cardPos2 = getCardLabel(new Card(
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 3) {
                    cardPos3 = getCardLabel(new Card(
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                }
            }
        }

        letzterStich = new JFrame("letzter Stich");
        letzterStich.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                letzterStich=null;
            }
        });
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

    private void handleUserPanelUpdate(RequestObject object) {

        int ownNumber = players.indexOf(c.connection.name);
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
        int otherNumber = players.indexOf(object.getParams().get("player").getAsString());

        switch (tmpList.indexOf(otherNumber)){
            case 1:{
                userLabel_1.setText(createUserLabelString(
                        object.getParams().get("text").getAsString(),
                        object.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 2:{
                userLabel_2.setText(createUserLabelString(
                        object.getParams().get("text").getAsString(),
                        object.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 3:{
                userLabel_3.setText(createUserLabelString(
                        object.getParams().get("text").getAsString(),
                        object.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 0:{
                if(object.getParams().get("player").getAsString().equals(c.connection.name)){
                    userLabel_4.setText(createUserLabelString(
                            object.getParams().get("text").getAsString(),
                            "Du",
                            false));
                }
                else{
                    userLabel_4.setText(createUserLabelString(
                            object.getParams().get("text").getAsString(),
                            object.getParams().get("player").getAsString(),
                            true));
                }
                break;
            }
        }
        updateTable();
    }

    //


    // helper functions, game specific functions


    private void selectCards4Armut() {
        controlPanel.removeAll();
        autoSelectArmutCards();
        setSendCardButton(SendCards.RICH, "Armut anbieten");
    }

    private void setSendCardButton(String receiver, String buttonText) {
        controlPanel.removeAll();
        sendCardsButton = new JButton(buttonText);
        sendCardsButton.addActionListener(e -> {
            handler.queueOutMessage(new SendCards(Arrays.asList(exchangeCards), receiver));
            selectCards = false;
            controlPanel.setVisible(false);
            middlePanel.removeAll();
            middlePanel.setVisible(false);
            clearPlayArea();
        });
        controlPanel.add(sendCardsButton);
        controlPanel.setVisible(true);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void autoSelectArmutCards() {
         middlePanel.removeAll();
         List<BaseCard> cards = hand.stream().filter(card->card.trumpf).collect(Collectors.toList());
         exchangeCards = new BaseCard[cards.size()];
         cLabels = new JLabel[cards.size()];
         for(int i =0; i < cards.size();i++){
             cLabels[i] = new JLabel();
         }
         cards.forEach(c-> moveCard2Exchange(c,true));
         middlePanel.setVisible(true);
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
    protected void uiTest() {
        hand = new ArrayList<>();
        List<Card> list = Card.createCardList();
        while(hand.size()<10){
            hand.add(Card.randomCard(list,random));
        }
        createCardButtons(hand);
        setGameSpecificButtons(hand);

        userLabel_1.setText("Spieler 1");
        userLabel_2.setText("Spieler 2");
        userLabel_3.setText("Spieler 3");
        userLabel_4.setText("Spieler 4");
        serverMessageLabel.setText("Dieses Feld wird vom Server verwendet");
        gameMessageLabel.setText("Dieses Feld wird vom Client verwendet");
        tableStich = new HashMap<>();
        for(int i = 0;i<4;i++){
            tableStich.put(i,Card.randomCard(list,random));
        }
    }


    @Override
    protected void moveCard2Hand(BaseCard card) {
        hand.add(card);
        hand = SortHand.sort(hand,selectedGame,true);
        createCardButtons(hand);

    }
}
