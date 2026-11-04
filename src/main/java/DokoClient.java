import base.*;
import base.doko.Card;
import base.doko.DokoCards;
import base.doko.SortHand;
import base.doko.assist.Assist;
import base.doko.messages.*;
import base.messages.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static base.doko.messages.GameSelected.GAMES.*;

public class DokoClient extends BaseClient implements IInputputHandler, IDialogInterface {

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
    private GameSelected.GAMES selectedGame = NORMAL;
    private DokoEndDialog endDialog;
    private Card mustPlay = null;
    private Assist assist;


    public DokoClient(ComClient handler, List<String> players, Configuration c){
        super(handler,players,c);
        currentGame = Statics.game.DOKO;
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
            selectedGame = NORMAL;
            setTrumpfCards();
        });
        sortDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortDamenSolo(hand));
            deselectAllSortButtons();
            sortDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.DAMEN;
            setTrumpfCards();
        });
        sortBuben.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenSolo(hand));
            deselectAllSortButtons();
            sortBuben.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.BUBEN;
            setTrumpfCards();
        });
        sortBubenDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenDamenSolo(hand));
            deselectAllSortButtons();
            sortBubenDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.BUBENDAMEN;
            setTrumpfCards();
        });
        sortFleischlos.addActionListener(e->{
            createCardButtons(hand=SortHand.sortFleischlos(hand));
            deselectAllSortButtons();
            sortFleischlos.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.FLEISCHLOS;
            setTrumpfCards();
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.KREUZ;
            setTrumpfCards();
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.PIK;
            setTrumpfCards();
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.HERZ;
            setTrumpfCards();
        });
        sortKaro.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
            selectedGame = KARO;
            setTrumpfCards();
        });
        sortArmut.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortArmut(hand, schweinExists));
            deselectAllSortButtons();
            sortArmut.setBackground(Color.GREEN);
            selectedGame = ARMUT;
            setTrumpfCards();
        });
        sortKoenige.addActionListener(e->{
            createCardButtons(hand=SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKoenige.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.KOENIGE;
            setTrumpfCards();
        });
        sortHochzeit.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortNormal(hand,schweinExists));
            deselectAllSortButtons();
            sortHochzeit.setBackground(Color.GREEN);
            selectedGame = GameSelected.GAMES.HOCHZEIT;
            setTrumpfCards();
        });
    }



    private void setTrumpfCards() {
        if(hand!=null) {
            hand.forEach(card -> card.trumpf = Card.isTrumpf(card, selectedGame));
        }
        new Assist(hand,selectedGame);
    }


    @Override
    protected void setGameSpecificButtons(List<BaseCard> cards){

        controlPanel.removeAll();
        JButton vorbehalt = new JButton("OK");
        vorbehalt.addActionListener(e ->{
            handler.queueOutMessage(new GameSelected(players.indexOf(c.connection.name),selectedGame));
            controlPanel.setVisible(false);
            controlPanel.removeAll();
            layeredPane.moveToFront(hud);
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
                Card card = cardMap.get(label);
                if (selectCards) {
                    moveCard2Exchange(card);
                    if(hand.size()==10){
                        setSendCardButton(SendCards.POOR, "Karten zurueckgeben");
                    }
                    else{
                        controlPanel.removeAll();
                    }
                } else {
                    if (wait4Player) {
                        if(c.doko.regeln.bedienen){
                            if(mustPlay==null || mustPlay.farbe.equals(card.farbe)) {
                                wait4Player = false;
                                hand.remove(card);
                                label.setVisible(false);
                                handler.queueOutMessage(new PutCard(players.indexOf(c.connection.name),card));
                                if (c.ui.redrawCards) {
                                    createCardButtons(hand);
                                }
                            }
                            else{
                                System.out.println("Nicht bedient");
                            }
                        }
                        else{
                            wait4Player = false;
                            hand.remove(card);
                            label.setVisible(false);
                            handler.queueOutMessage(new PutCard(players.indexOf(c.connection.name),card));
                            if (c.ui.redrawCards) {
                                createCardButtons(hand);
                            }
                        }
                    }
                }
            }
        };
        exchangeCardClickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                Card card = null;
                for(int i = 0; i<exchangeCards.length;i++){
                    if(cLabels[i]!=null && cLabels[i]==label){
                        card = exchangeCards[i];
                        break;
                    }
                }
                moveCard2Hand(card);
                if(hand.size()==10){
                    setSendCardButton(SendCards.POOR, "Karten zurueckgeben");
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
                hudMiddle.revalidate();
                hudMiddle.repaint();
                hud.revalidate();
                hud.repaint();
                layeredPane.revalidate();
                layeredPane.repaint();
                overLayer.revalidate();
                overLayer.repaint();
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
            case GameEnd.COMMAND: {
                handleGameEnd(message);
                break;
            }
            case SelectGame.COMMAND: {
                controlPanel.setVisible(players.indexOf(c.connection.name) != spectator);
                break;
            }
            case GameSelected.COMMAND: {
                handleGameSelected(message);
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
            case PlayersInLobby.COMMAND:{
                handlePlayersInLobby(message);
                break;
            }
            case Acknowledge.COMMAND:{
                endDialog.ackowledge();
            }
            default:
                break;
        }
    }

    // handle messages
    private void handlePlayersInLobby(RequestObject message) {
        DefaultListModel<String> model = new DefaultListModel<>();
        players.clear();
        message.getParams().getAsJsonArray("players").forEach(player ->
                players.add(player.getAsString()));
        model.addAll(players);
    }

    private void handlePutCard(RequestObject message){
        try {
            middlePanel.setBackground(new Color(0,0,0,0));
            middlePanel.setOpaque(false);
            int ownNumber = players.indexOf(c.connection.name);
            List<Integer> tmpList = new ArrayList<>();
            int i = ownNumber;
            log.info("own:" +i);

            while (tmpList.size() < 4) {
                if (i != spectator) {
                    tmpList.add(i);
                    log.info("added:" +i);
                }
                else {
                    log.info("not added:" +i);
                }
                i++;
                if (i > players.size() - 1) {
                    i = 0;
                }
            }

            if (currentCardsOnTable > 3) {
                clearPlayArea();
                if (letzterStich != null) {
                    letzterStich.dispose();
                    letzterStich = null;
                }
                updateTable();
                currentCardsOnTable = 0;
                mustPlay = null;
                tableStich.clear();
            }

            log.info("player:" + i);
            int playerNumber = message.getParams().get("player").getAsInt();
            Card card = new Card(message.getParams().get("wert").getAsString(),
                    message.getParams().get("farbe").getAsString());
            if(currentCardsOnTable ==0){
                mustPlay = card;
            }

            for (int j : tmpList) {
                if (playerNumber == j) {
                    log.info(tmpList.indexOf(j) + ":" + card);
                    drawCard2Position(card, tmpList.indexOf(j), table.getHeight(), table.getWidth());
                    tableStich.put(tmpList.indexOf(j), card);
                    break;
                }
            }
            currentCardsOnTable++;
            updateTable();
            if(assist!=null){
                assist.putCard(card);
            }
        }
        catch (Exception ex){
            log.error(ex.toString());
        }
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



    private void handleGetArmut() {
        if (JOptionPane.showConfirmDialog(mainFrame, "Armut mitnehmen?",
                "Armut mitnehmen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            handler.queueOutMessage(new GetArmut(players.indexOf(c.connection.name), true));
        } else {
            handler.queueOutMessage(new GetArmut(players.indexOf(c.connection.name), false));
        }
    }

    private void handleSendCards(RequestObject message) {
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        SendCards messageSendCards = new SendCards(message);
        boolean isRich = messageSendCards.getReceiver().equals(SendCards.RICH);
        middlePanel.removeAll();
        selectCards = isRich;
        exchangeCards = messageSendCards.getCards().toArray(new Card[0]);

        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0;i<exchangeCards.length;i++){
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            if(isRich) {
                cLabels[i].addMouseListener(exchangeCardClickAdapter);
            }
            middlePanel.add(cLabels[i]);
        }

        if(isRich){
            setSendCardButton(SendCards.POOR, "Karten zurueckgeben");
        }
        else {
            setAcceptArmutReturn();
        }

        middlePanel.setVisible(true);
        middlePanel.revalidate();
        middlePanel.repaint();

    }

    private void setAcceptArmutReturn() {
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        controlPanel.removeAll();
        JButton button = new JButton("Karten aufnehmen");
        button.addActionListener(e -> {
            for (Card exchangeCard : exchangeCards) {
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

    private void handleGameSelected(RequestObject message) {
        selectedGame = GameSelected.GAMES.getName(message.getParams().get("game").getAsInt());
        if (hand != null && hand.size() > 0) {
            hand = SortHand.sort(hand,selectedGame,schweinExists);
            createCardButtons(hand);
            if (selectedGame==NORMAL
                    || selectedGame==KARO
                    || selectedGame==ARMUT) {
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
        if (assist!=null){
            assist.close();
        }
        assist =  new Assist(hand,selectedGame);
    }

    private void handleGameEnd(RequestObject message) {
        updateTable();
        endDialog = new DokoEndDialog(this,
                message.getParams().get("re1").getAsString(),
                message.getParams().get("re2").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("kontra2").getAsString(),
                message.getParams().get("remain").getAsInt());
        endDialog.showDialog(this.mainFrame);

    }

    @Override
    public void quitEnd(){
        clearPlayArea();
        currentCardsOnTable = 0;
        tableStich.clear();
        schweinExists = false;
        selectCards = false;
        wait4Player = false;
        hand = new ArrayList<>();
        serverMessages = new ArrayList<>();
        displayAllServerMessages();
        handler.queueOutMessage(new ReadyForNextRound(players.indexOf(c.connection.name)));
    }

    @Override
    protected void handleCards(RequestObject message) {
        selectedGame = NORMAL;
        hand = new ArrayList<>();
        message.getParams().getAsJsonArray("cards").forEach(card -> {
            String[] parts = card.getAsString().split(" ", 2);
            hand.add(new Card(parts[1], parts[0]));
        });
        super.handleCards(message);

        new Assist(hand,NORMAL);
        new Assist(hand,DAMEN);
        new Assist(hand,BUBEN);
        new Assist(hand,BUBENDAMEN);
        new Assist(hand,FLEISCHLOS);
        new Assist(hand,KREUZ);
        new Assist(hand,PIK);
        new Assist(hand,HERZ);
        new Assist(hand,KARO);
    }

    private void handleLastStich(RequestObject message) {
        handleLastStich(message,4,spectator);
    }

    private void handleUserPanelUpdate(RequestObject message) {
        String playerName = message.getParams().get("player").getAsString();
        String text = message.getParams().get("text").getAsString();
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

        int otherNumber = players.indexOf(playerName);

        switch (tmpList.indexOf(otherNumber)){
            case 1:
            case 2:
            case 3:
                userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                        text,
                        playerName,
                        true));
                break;
            case 0:{
                if(playerName.equals(c.connection.name)){
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                             text,
                            "Du",
                            false));
                }
                else{
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            text,
                            playerName,
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
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        controlPanel.removeAll();
        autoSelectArmutCards();
        setSendCardButton(SendCards.RICH, "Armut anbieten");
    }

    private void setSendCardButton(String receiver, String buttonText) {
        controlPanel.removeAll();
        JButton sendCardsButton = new JButton(buttonText);
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
         List<Card> cards = hand.stream().filter(card->card.trumpf).collect(Collectors.toList());
         exchangeCards = new Card[cards.size()];
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
    protected void moveCard2Hand(BaseCard card) {
        hand.add(card);
        hand = SortHand.sort(hand,selectedGame,true);
        createCardButtons(hand);

    }
}
