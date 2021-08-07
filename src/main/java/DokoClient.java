import base.*;
import base.doko.DokoCards;
import base.doko.SortHand;
import base.doko.messages.*;
import base.messages.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static base.DokoConfig.BEDIENEN;
import static base.doko.messages.MessageGameSelected.GAMES.*;

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
    private MessageGameSelected.GAMES selectedGame = NORMAL;
    private int spectator;
    private int aufspieler;
    private int currentCardsOnTable = 0;

    private DokoEndDialog endDialog;
    private BaseCard mustPlay = null;


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
            selectedGame = NORMAL;
            setTrumpfCards();
        });
        sortDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortDamenSolo(hand));
            deselectAllSortButtons();
            sortDamen.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.DAMEN;
            setTrumpfCards();
        });
        sortBuben.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenSolo(hand));
            deselectAllSortButtons();
            sortBuben.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.BUBEN;
            setTrumpfCards();
        });
        sortBubenDamen.addActionListener(e->{
            createCardButtons(hand=SortHand.sortBubenDamenSolo(hand));
            deselectAllSortButtons();
            sortBubenDamen.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.BUBENDAMEN;
            setTrumpfCards();
        });
        sortFleischlos.addActionListener(e->{
            createCardButtons(hand=SortHand.sortFleischlos(hand));
            deselectAllSortButtons();
            sortFleischlos.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.FLEISCHLOS;
            setTrumpfCards();
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.KREUZ;
            setTrumpfCards();
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.PIK;
            setTrumpfCards();
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.HERZ;
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
            selectedGame = MessageGameSelected.GAMES.KOENIGE;
            setTrumpfCards();
        });
        sortHochzeit.addActionListener(e -> {
            createCardButtons(hand=SortHand.sortNormal(hand,schweinExists));
            deselectAllSortButtons();
            sortHochzeit.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.HOCHZEIT;
            setTrumpfCards();
        });
    }



    private void setTrumpfCards() {
        if(hand!=null) {
            hand.forEach(card -> card.trump = DokoCards.isTrumpf(card, selectedGame));
            gameMessageLabel.setText(String.valueOf(hand.stream().filter(card -> card.trump).count()));
        }
    }


    @Override
    protected void setGameSpecificButtons(List<BaseCard> cards){

        controlPanel.removeAll();
        JButton vorbehalt = new JButton("OK");
        vorbehalt.addActionListener(e ->{
            handler.queueOutMessage(new MessageGameSelected(players.indexOf(c.connection.name),selectedGame));
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

        if(cards.stream().filter(p->p.trump).count()<4){
            controlPanel.add(sortArmut);
        }
        if(cards.stream().filter(p->p.kind.equals(Statics.KOENIG)).count()>4) {
            controlPanel.add(sortKoenige);
        }
        if(cards.stream().filter(p->p.kind.equals(Statics.DAME)&&p.suit.equals(Statics.KREUZ)).count()>1){
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
                BaseCard card = cardMap.get(label);
                if (selectCards) {
                    moveCard2Exchange(card);
                    if(hand.size()==10){
                        setSendCardButton(MessageSendCards.POOR, "Karten zurückgeben");
                    }
                    else{
                        controlPanel.removeAll();
                    }
                } else {
                    if (wait4Player || test) {
                        if((boolean)c.doko.regeln.get(BEDIENEN)
                                || mustPlay==null
                                || mustPlay.suit.equals(card.suit)) {
                            wait4Player = false;
                            hand.remove(card);
                            label.setVisible(false);
                            handler.queueOutMessage(new MessagePutCard(players.indexOf(c.connection.name), card.suit, card.kind));
                            if (c.ui.redrawCards) {
                                createCardButtons(hand);
                            }
                        }
                        else{
                            //TODO:
                            System.out.println("nicht bedient (simple test");
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
                    setSendCardButton(MessageSendCards.POOR, "Karten zurückgeben");
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
    public void handleInput(Message message) {
        super.handleInput(message);
        switch (message.getCommand()) {
            case MessageCards.COMMAND: {
                deselectAllSortButtons();
                handleCards(message);
                break;
            }
            case MessagePutCard.COMMAND: {
                handlePutCard(message);
                break;
            }
            case MessageCurrentStich.LAST: { }
            case MessageCurrentStich.SPECIFIC: {
                if(letzterStich==null) {
                    handleLastStich(message);
                }
                break;
            }
            case MessageGameEnd.COMMAND: {
                handleGameEnd(message);
                break;
            }
            case MessageSelectGame.COMMAND: {
                controlPanel.setVisible(players.indexOf(c.connection.name) != spectator);
                break;
            }
            case MessageGameType.COMMAND: {
                handleGameType(message);
                break;
            }
            case MessageSelectCards4Armut.COMMAND: {
                selectCards4Armut();
                break;
            }
            case MessageSendCards.COMMAND: {
                handleSendCards(message);
                break;
            }
            case MessageGetArmut.COMMAND: {
                handleGetArmut();
                break;
            }
            case MessageUpdateUserPanel.COMMAND: {
                handleUserPanelUpdate(message);
                break;
            }
            case MessageAnnounceSpectator.COMMAND: {
                handleAnnounceSpectator(message);
                break;
            }
            case MessagePlayerList.IN_LOBBY:{
                handlePlayersInLobby(message);
                break;
            }
            case MessageAcknowledge.COMMAND:{
                endDialog.ackowledge();
            }
            default:
                break;
        }
    }

    // handle messages
    private void handlePlayersInLobby(Message message) {
        MessagePlayerList messagePlayersInLobby = new MessagePlayerList(message);
        DefaultListModel<String> model = new DefaultListModel<>();
        players.clear();
        players.addAll(messagePlayersInLobby.getPlayerNamesList());
        model.addAll(players);
    }

    private void handlePutCard(Message message){
        try {
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

            MessagePutCard messagePutCard = new MessagePutCard(message);
            int player = messagePutCard.getPlayerNumber();
            log.info("player:" + i);
            BaseCard card = messagePutCard.getCard();
            card.trump = DokoCards.isTrumpf(card,selectedGame);
            if(currentCardsOnTable ==0){
                mustPlay = card;
            }

            for (int j : tmpList) {
                if (player == j) {
                    log.info(tmpList.indexOf(j) + ":" + card);
                    drawCard2Position(card, tmpList.indexOf(j), table.getHeight(), table.getWidth());
                    tableStich.put(tmpList.indexOf(j), card);
                    break;
                }
            }
            currentCardsOnTable++;
            updateTable();
        }
        catch (Exception ex){
            log.error(ex.toString());
        }
    }

    private void handleAnnounceSpectator(Message message) {
        MessageAnnounceSpectator messageAnnounceSpectator = new MessageAnnounceSpectator(message);
        spectator = messageAnnounceSpectator.getSpectatorNumber();
        aufspieler = messageAnnounceSpectator.getStarterNumber();
        if(players.size()>4 && players.get(spectator).equals(c.connection.name)) {
            handler.queueOutMessage(new MessageDisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }



    private void handleGetArmut() {
        if (JOptionPane.showConfirmDialog(mainFrame, "Armut mitnehmen?",
                "Armut mitnehmen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            handler.queueOutMessage(new MessageGetArmut(players.indexOf(c.connection.name), true));
        } else {
            handler.queueOutMessage(new MessageGetArmut(players.indexOf(c.connection.name), false));
        }
    }

    private void handleSendCards(Message message) {
        MessageSendCards messageSendCards = new MessageSendCards(message);
        boolean isRich = messageSendCards.getReceiver().equals(MessageSendCards.RICH);
        middlePanel.removeAll();
        selectCards = isRich;
        exchangeCards = messageSendCards.getCards().toArray(new BaseCard[0]);

        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0;i<exchangeCards.length;i++){
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            if(isRich) {
                cLabels[i].addMouseListener(exchangeCardClickAdapter);
            }
            middlePanel.add(cLabels[i]);
        }

        if(isRich){
            setSendCardButton(MessageSendCards.POOR, "Karten zurückgeben");
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
            handler.queueOutMessage(new MessageCardsReturned());
            middlePanel.removeAll();
            middlePanel.setVisible(false);
            controlPanel.removeAll();
            controlPanel.setVisible(false);
            clearPlayArea();
        });
        controlPanel.add(button);
        controlPanel.setVisible(true);
    }

    private void handleGameType(Message message) {
        MessageGameType messageGameType = new MessageGameType(message);
        selectedGame = messageGameType.getSelectedGame();
        if (hand != null && hand.size() > 0) {
            hand = SortHand.sort(hand,selectedGame,schweinExists);
            createCardButtons(hand);
            if (selectedGame==NORMAL
                    || selectedGame==KARO
                    || selectedGame==ARMUT) {
                if (hand.stream().filter(p -> p.suit.equals(Statics.KARO)
                        && p.kind.equals(Statics.ASS)).count() > 1) {
                    handler.queueOutMessage(new MessageSchweinExists());
                    schweinExists = true;
                } else {
                    schweinExists = false;
                }
            }
        }
        aufspieler = -1;
    }

    private void handleGameEnd(Message message) {
        updateTable();
        endDialog = new MessageGameEnd(message).getEndDialog(this);
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
        handler.queueOutMessage(new MessageReadyForNextRound(players.indexOf(c.connection.name)));
    }

    @Override
    protected void handleCards(Message message) {
        MessageCards messageCards = new MessageCards(message);
        selectedGame = NORMAL;
        hand = messageCards.getCards();
        super.handleCards(message);
    }

    private void handleLastStich(Message message) {
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

        MessageCurrentStich messageCurrentStich = new MessageCurrentStich(message);
        Map<Integer,BaseCard> map = messageCurrentStich.GetStichMap();
        for(int j = 0;j<tmpList.size();j++){
            if (map.containsKey(tmpList.get(j))) {
                if (j == 0) {
                    cardPos4 = getCardLabel(map.get(tmpList.get(j)));
                } else if (j == 1) {
                    cardPos1 =  getCardLabel(map.get(tmpList.get(j)));
                } else if (j == 2) {
                    cardPos2 =  getCardLabel(map.get(tmpList.get(j)));
                } else if (j == 3) {
                    cardPos3 =  getCardLabel(map.get(tmpList.get(j)));
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

    private void handleUserPanelUpdate(Message message) {
        MessageUpdateUserPanel messageUpdateUserPanel = new MessageUpdateUserPanel(message);
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

        int otherNumber = players.indexOf(messageUpdateUserPanel.getPlayerName());

        switch (tmpList.indexOf(otherNumber)){
            case 1:
            case 2:
            case 3:
                userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                        messageUpdateUserPanel.getText(),
                        messageUpdateUserPanel.getPlayerName(),
                        true));
                break;
            case 0:{
                if(messageUpdateUserPanel.getPlayerName().equals(c.connection.name)){
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                             messageUpdateUserPanel.getText(),
                            "Du",
                            false));
                }
                else{
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            messageUpdateUserPanel.getText(),
                            messageUpdateUserPanel.getPlayerName(),
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
        setSendCardButton(MessageSendCards.RICH, "Armut anbieten");
    }

    private void setSendCardButton(String receiver, String buttonText) {
        controlPanel.removeAll();
        sendCardsButton = new JButton(buttonText);
        sendCardsButton.addActionListener(e -> {
            handler.queueOutMessage(new MessageSendCards(Arrays.asList(exchangeCards), receiver));
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
         List<BaseCard> cards = hand.stream().filter(card->card.trump).collect(Collectors.toList());
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
    protected void moveCard2Hand(BaseCard card) {
        hand.add(card);
        hand = SortHand.sort(hand,selectedGame,true);
        createCardButtons(hand);

    }
}
