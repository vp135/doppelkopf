import base.*;
import base.messages.*;
import base.skat.SortHand;
import base.skat.messages.*;
import com.google.gson.JsonArray;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SkatClient extends BaseClient implements IInputputHandler, IDialogInterface {


    private JButton sortKaro;
    private JButton sortHerz;
    private JButton sortPik;
    private JButton sortKreuz;
    private JButton sortNull;
    private JButton sortGrand;
    private MessageGameSelected.GAMES selectedGame;
    private boolean handSpiel;
    private boolean ouvert;
    private int spectator;
    private int currentCardsOnTable;
    private int aufspieler;
    private JButton nextValue;
    private JButton pass;

    private JButton button_skat;
    private JButton button_ok;
    private JButton button_karo;
    private JButton button_herz;
    private JButton button_pik;
    private JButton button_kreuz;
    private JButton button_null;
    private JButton button_grand;
    private JButton button_ouvert;
    private JPanel buttonsPanel;
    private List<BaseCard> ouvertCards;
    private Map<BaseCard,JLabel> ouvertLabelMap;
    private Map<JLabel,BaseCard> ouvertCardMap;
    private JPanel ouvertPanel;
    private boolean selectGame;
    private SkatEndDialog endDialog;

    private MessageGameSelected.GAMES sortGame = null;


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
        pass = new JButton("weg");
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
            createCardButtons(hand = SortHand.sortKaro(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Karo;
            sortKaro.setBackground(Color.GREEN);
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Herz;
            sortHerz.setBackground(Color.GREEN);
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Pik;
            sortPik.setBackground(Color.GREEN);
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Kreuz;
            sortKreuz.setBackground(Color.GREEN);
        });
        sortNull.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortNull(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Null;
            sortNull.setBackground(Color.GREEN);
        });
        sortGrand.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortGrand(hand));
            deselectAllSortButtons();
            sortGame = MessageGameSelected.GAMES.Grand;
            sortGrand.setBackground(Color.GREEN);
        });
        pass.addActionListener(e -> {
            handler.queueOutMessage(new MessagePassen(c.connection.name));
            nextValue.setVisible(false);
            pass.setVisible(false);
        });

        nextValue.setVisible(false);
        pass.setVisible(false);
    }


    @Override
    protected void deselectAllSortButtons() {
        super.deselectAllSortButtons();
        sortGame = null;
    }

    @Override
    protected void setGameSpecificButtons(List<BaseCard> hand) {

        controlPanel.removeAll();
        buttonList.forEach(button -> controlPanel.add(button));
        Dimension d = new Dimension(mainFrame.getSize().width, mainFrame.getSize().height / (30));
        setComponentSizes(controlPanel, d);
    }

    @Override
    protected void setGameSpecifics() {
        maxHandCards = 12;
        heightCorrection = 1.1f;
    }

    @Override
    protected void redrawEverything() {
        super.redrawEverything();
        if (hand != null) {
            createCardButtons(hand);
            clearPlayArea();
            tableStich.keySet().forEach(i ->{
                if(i==2){
                    drawCard2Position(tableStich.get(i), 3, table.getHeight(), table.getWidth());
                }
                else {
                    drawCard2Position(tableStich.get(i), i, table.getHeight(), table.getWidth());
                }
            });
        }
    }

    @Override
    protected void setCardClickAdapter() {
        handCardClickAdapter = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                BaseCard card = cardMap.get(label);
                if (selectCards) {
                    moveCard2Exchange(card);
                    setAnsageButtonState();
                } else if (wait4Player) {
                    wait4Player = false;
                    hand.remove(card);
                    label.setVisible(false);
                    handler.queueOutMessage(new MessagePutCard(players.indexOf(c.connection.name), card.suit, card.kind));
                    //serverMessageLabel.setText("");
                    if (c.ui.redrawCards) {
                        createCardButtons(hand);
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
                button_ok.setEnabled(hand.size()==10);
                for(int i = 0;i<exchangeCards.length;i++){
                    if(exchangeCards[i]==card){
                        exchangeCards[i] = null;
                        cLabels[i].setVisible(false);
                        break;
                    }
                }
                //middlePanel.revalidate();
                //middlePanel.repaint();
            }
        };
    }

    @Override
    protected void moveCard2Hand(BaseCard card) {
        super.moveCard2Hand(card);
        if(sortGame!=null) {
            hand = SortHand.sort(hand, sortGame);
            createCardButtons(hand);
        }
    }

    private void setAnsageButtonState() {
        if(hand.size()==10){
            if(selectedGame== MessageGameSelected.GAMES.Ramsch){
                button_ok.setEnabled(!selectGame);
            }
            else{
                button_ok.setEnabled(true);
            }
        }
    }

    @Override
    public void handleInput(Message message) {
        super.handleInput(message);
        try {
            switch (message.getCommand()) {
                case MessageCards.COMMAND:
                    deselectAllSortButtons();
                    handleCards(message);
                    break;
                case MessagePutCard.COMMAND:
                    handlePutCard(message);
                    break;
                case MessageCurrentStich.LAST:
                case MessageCurrentStich.SPECIFIC:
                    if (letzterStich == null) {
                        handleLastStich(message);
                    }
                    break;
                case MessageGameEnd.COMMAND:
                    handleGameEnd(message);
                    break;
                case MessageUpdateUserPanel.COMMAND:
                    handleUserPanelUpdate(message);
                    break;
                case MessageAnnounceSpectator.COMMAND:
                    handleAnnounceSpectator(message);
                    break;
                case MessageReizen.COMMAND:
                    handleReizen(message);
                    break;
                case MessageSelectGame.COMMAND:
                    handleSelectGame();
                    break;
                case MessageGameSelected.COMMAND:
                    handleGameSelected(message);
                    break;
                case MessageSkat.COMMAND:
                    handleSkat(message);
                    break;
                case MessageRamschSkat.COMMAND:
                    handleRamschSkat();
                    break;
                case MessageOuvertCards.COMMAND:
                    handleOuvertCards(message);
                    break;
                case MessageGrandHand.COMMAND:
                    handleGrandHand(message);
                    break;
                case MessageAcknowledge.COMMAND:
                    endDialog.ackowledge();
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void handleGrandHand(Message message) {
        gameMessageLabel.setText("Willst du einen Grand hand spielen?");
        middlePanel.removeAll();
        JButton gh = new JButton("Grand hand");
        JButton ramsch = new JButton("Ramsch");

        gh.addActionListener(e ->{
            handler.queueOutMessage(new MessageGrandHand(c.connection.name,true));
            middlePanel.removeAll();
            middlePanel.revalidate();
            middlePanel.repaint();
            gameMessageLabel.setText("");
        });
        ramsch.addActionListener(e ->{
            handler.queueOutMessage(new MessageGrandHand(c.connection.name,false));
            middlePanel.removeAll();
            middlePanel.revalidate();
            middlePanel.repaint();
            gameMessageLabel.setText("");
        });

        middlePanel.add(gh);
        middlePanel.add(new JLabel());
        middlePanel.add(ramsch);
        middlePanel.revalidate();
        middlePanel.repaint();
    }

    private void handleOuvertCards(Message message) {
        JsonArray array = message.getParams().getAsJsonArray("cards");
        ouvertCards = new ArrayList<>();
        array.forEach(card -> {
            BaseCard c = new BaseCard(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            ouvertCards.add(c);
        });
        createOuvertPanel(ouvertCards);
    }

    private void handleGameSelected(Message message) {
        selectedGame = MessageGameSelected.GAMES.valueOf(message.getParams().get("game").getAsString());
        createCardButtons(hand = SortHand.sort(hand,selectedGame));
        controlPanel.setVisible(false);
        ouvert = message.getParams().get("ouvert").getAsBoolean();
    }

    private void handleSkat(Message message) {
        middlePanel.removeAll();
        selectCards = true;
        JsonArray array = message.getParams().getAsJsonArray("cards");
        exchangeCards = new BaseCard[array.size()];
        for (int i = 0; i< exchangeCards.length;i++){
            exchangeCards[i]= new BaseCard(array.get(i).getAsString().split(" ")[1], array.get(i).getAsString().split(" ")[0]);
        }

        middlePanel.add(buttonsPanel);

        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0;i<exchangeCards.length;i++){
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            cLabels[i].addMouseListener(exchangeCardClickAdapter);
            middlePanel.add(cLabels[i]);
        }

        button_ok.setVisible(true);

        middlePanel.revalidate();
        middlePanel.repaint();
    }

    private void handleRamschSkat() {
        middlePanel.removeAll();
        buttonsPanel = new JPanel(new GridLayout(1,1));
        button_ok = new JButton("Ok");
        button_ok.setVisible(false);
        button_skat = new JButton("Skat aufnehmen");
        JButton button_schieben = new JButton("Schieben");

        button_ok.addActionListener(e -> {
            handler.queueOutMessage(new MessageSkat(c.connection.name, Arrays.asList(exchangeCards)));
            middlePanel.removeAll();
            middlePanel.repaint();
            selectCards = false;
        });


        button_skat.addActionListener(e -> {
            middlePanel.remove(button_schieben);
            selectCards = true;
            button_ok.setVisible(true);
            handler.queueOutMessage(new MessageGetSkat(c.connection.name));
        });

        button_schieben.addActionListener(e -> {
            handler.queueOutMessage(new MessageSchieben(c.connection.name));
            middlePanel.removeAll();
            middlePanel.revalidate();
            middlePanel.repaint();
        });

        buttonsPanel.add(button_ok);
        middlePanel.add(buttonsPanel);
        middlePanel.add(button_skat);
        middlePanel.add(button_schieben);
        middlePanel.revalidate();
        middlePanel.repaint();
    }

    private void handleSelectGame() {
        handSpiel = true;
        selectGame = true;
        middlePanel.removeAll();
        middlePanel.setBackground(Color.BLACK);
        buttonsPanel = new JPanel(new GridLayout(5, 2));
        button_karo = new JButton("Karo");
        button_herz = new JButton("Herz");
        button_pik = new JButton("Pik");
        button_kreuz = new JButton("Kreuz");
        button_null = new JButton("Null");
        button_grand = new JButton("Grand");
        button_ouvert = new JButton("Ouvert");

        button_karo.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Karo;
            deselectGameButtons();
            setOuvertButtonState();
            button_karo.setBackground(Color.GREEN);
        });
        button_herz.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Herz;
            deselectGameButtons();
            setOuvertButtonState();
            button_herz.setBackground(Color.GREEN);
        });
        button_pik.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Pik;
            deselectGameButtons();
            setOuvertButtonState();
            button_pik.setBackground(Color.GREEN);
        });
        button_kreuz.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Kreuz;
            deselectGameButtons();
            setOuvertButtonState();
            button_kreuz.setBackground(Color.GREEN);
        });
        button_null.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Null;
            deselectGameButtons();
            setOuvertButtonState();
            button_null.setBackground(Color.GREEN);
        });
        button_grand.addActionListener(e -> {
            selectedGame = MessageGameSelected.GAMES.Grand;
            deselectGameButtons();
            setOuvertButtonState();
            button_grand.setBackground(Color.GREEN);
        });


        button_ouvert.addActionListener(e -> {
            if (ouvert) {
                ouvert = false;
                button_ouvert.setBackground(Color.BLACK);
            } else {
                ouvert = true;
                button_ouvert.setBackground(Color.GREEN);
            }
        });

        button_ok = new JButton("Ansagen");
        button_ok.setEnabled(false);
        buttonsPanel.add(button_karo);
        buttonsPanel.add(button_herz);
        buttonsPanel.add(button_pik);
        buttonsPanel.add(button_kreuz);
        buttonsPanel.add(button_null);
        buttonsPanel.add(button_grand);
        buttonsPanel.add(button_ouvert);

        button_ok.addActionListener(e -> {
            handler.queueOutMessage(new MessageGameSelected(c.connection.name, selectedGame, handSpiel, ouvert));
            if (!handSpiel) {
                handler.queueOutMessage(new MessageSkat(c.connection.name, Arrays.asList(exchangeCards)));
            }
            middlePanel.removeAll();
            middlePanel.repaint();
            overLayer.moveToFront(configPanel);
            layeredPane.setLayer(table,0);
            table.setBackground(new Color(0,0,0,0));
            layeredPane.setLayer(hud,1);
            hud.setBackground(new Color(0,0,0,0));
            hud.setOpaque(true);
            middlePanel.setBackground(new Color(0,0,0,0));

            selectCards = false;
            selectGame = false;
        });

        buttonsPanel.add(button_ok);

        button_skat = new JButton("Skat aufnehmen");
        button_skat.addActionListener(e -> {
            handler.queueOutMessage(new MessageGetSkat(c.connection.name));
            selectCards = true;
            handSpiel = false;
            setOuvertButtonState();
        });

        middlePanel.add(buttonsPanel);
        middlePanel.add(button_skat);

        middlePanel.revalidate();
        middlePanel.repaint();
    }

    private void deselectGameButtons() {
        button_karo.setBackground(Color.BLACK);
        button_herz.setBackground(Color.BLACK);
        button_pik.setBackground(Color.BLACK);
        button_kreuz.setBackground(Color.BLACK);
        button_null.setBackground(Color.BLACK);
        button_grand.setBackground(Color.BLACK);
        if(selectedGame!= MessageGameSelected.GAMES.Ramsch){
            setAnsageButtonState();
        }
    }

    private void setOuvertButtonState() {
        if (!handSpiel) {
            if (selectedGame != MessageGameSelected.GAMES.Null) {
                button_ouvert.setEnabled(false);
                button_ouvert.setBackground(Color.BLACK);
                ouvert = false;
            } else {
                button_ouvert.setEnabled(true);
            }
        } else {
            button_ouvert.setEnabled(true);
        }
    }


    private void handleReizen(Message message) {
        int val = message.getParams().get("value").getAsInt();
        boolean active = message.getParams().get("active").getAsBoolean();
        if (active) {
            int nextVal = 0;
            if (val == 0) {
                nextVal = 18;
            } else {
                for (int i = 0; i < MessageReizen.VALUES.length; i++) {
                    if (MessageReizen.VALUES[i] == val) {
                        nextVal = MessageReizen.VALUES[i + 1];
                        break;
                    }
                }
            }
            nextValue.setText(String.valueOf(nextVal));
            for (ActionListener actionListener : nextValue.getActionListeners()) {
                nextValue.removeActionListener(actionListener);
            }
            nextValue.addActionListener(sagen);
        } else {
            nextValue.setText("Ja");

            for (ActionListener actionListener : nextValue.getActionListeners()) {
                nextValue.removeActionListener(actionListener);
            }
            nextValue.addActionListener(hoeren);
            pass.setText("weg");
        }
        nextValue.setVisible(true);
        pass.setVisible(true);
    }

    ActionListener sagen = e -> {
        MessageReizen reizen = new MessageReizen(c.connection.name, Integer.parseInt(nextValue.getText()), true);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    ActionListener hoeren = e -> {
        MessageReizen reizen = new MessageReizen(c.connection.name, 0, false);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    private void handleAnnounceSpectator(Message message) {
        spectator = message.getParams().get("player").getAsInt();
        aufspieler = message.getParams().get("starter").getAsInt();
        if (players.size() > 3 && players.get(spectator).equals(c.connection.name)) {
            handler.queueOutMessage(new MessageDisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    private void handleUserPanelUpdate(Message message) {
        int ownNumber = players.indexOf(c.connection.name);
        List<Integer> tmpList = new ArrayList<>();
        int i = ownNumber;
        while (tmpList.size() < 4) {
            if (i != spectator) {
                tmpList.add(i);
            }
            i++;
            if (i > players.size() - 1) {
                i = 0;
            }
        }
        int otherNumber = players.indexOf(message.getParams().get("player").getAsString());

        switch (tmpList.indexOf(otherNumber)) {
            case 1:
            case 2:
            case 3:
                userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            case 0: {
                if (message.getParams().get("player").getAsString().equals(c.connection.name)) {
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            message.getParams().get("text").getAsString(),
                            "Du",
                            false));
                } else {
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            message.getParams().get("text").getAsString(),
                            message.getParams().get("player").getAsString(),
                            true));
                }
                break;
            }
        }
        updateTable();
    }


    private void handleGameEnd(Message message) {
        updateTable();
        ouvertCards = new ArrayList<>();
        createOuvertPanel(ouvertCards);

        endDialog = new SkatEndDialog(
                this,
                selectedGame,
                message.getParams().get("re1").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("player1").getAsString(),
                message.getParams().get("player2").getAsString(),
                message.getParams().get("player3").getAsString(),
                message.getParams().get("remain").getAsInt());
        endDialog.showDialog(this.mainFrame);
    }


    @Override
    public void quitEnd() {
        currentCardsOnTable = 0;
        tableStich.clear();
        clearPlayArea();
        selectCards = false;
        wait4Player = false;
        hand = new ArrayList<>();
        serverMessages = new ArrayList<>();
        displayAllServerMessages();
        handler.queueOutMessage(new MessageReadyForNextRound(players.indexOf(c.connection.name)));
    }


    private void handleLastStich(Message message) {
        JLabel cardPos1 = new JLabel();
        JLabel cardPos2 = new JLabel();
        JLabel cardPos3 = new JLabel();
        JLabel cardPos4 = new JLabel();
        int ownNumber = players.indexOf(c.connection.name);
        List<Integer> tmpList = new ArrayList<>();
        int i = ownNumber;
        while (tmpList.size() < 3) {
            if (i != spectator) {
                tmpList.add(i);
            }
            i++;
            if (i > players.size() - 1) {
                i = 0;
            }
        }

        for (int j = 0; j < tmpList.size(); j++) {
            if (message.getParams().has(String.valueOf(tmpList.get(j)))) {
                if (j == 0) {
                    cardPos4 = getCardLabel(new BaseCard(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 1) {
                    cardPos1 = getCardLabel(new BaseCard(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 2) {
                    cardPos3 = getCardLabel(new BaseCard(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
                    );
                } else if (j == 3) {
                    cardPos2 = getCardLabel(new BaseCard(
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                            message.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0])
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
        JPanel jPanel = new JPanel(new GridLayout(3, 3));
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

    private void handlePutCard(Message message) {
        int ownNumber = players.indexOf(c.connection.name);
        List<Integer> tmpList = new ArrayList<>();
        int i = ownNumber;

        while (tmpList.size() < 3) {
            if (i != spectator) {
                tmpList.add(i);
            }
            i++;
            if (i > players.size() - 1) {
                i = 0;
            }
        }

        if (currentCardsOnTable > 2) {
            clearPlayArea();
            if (letzterStich != null) {
                letzterStich.dispose();
                letzterStich = null;
            }
            updateTable();
            currentCardsOnTable = 0;
            tableStich.clear();
        }

        int player = message.getParams().get("player").getAsInt();
        BaseCard card = new BaseCard(
                message.getParams().get("wert").getAsString(),
                message.getParams().get("farbe").getAsString());

        removeOuvertCard(card);

        for (int j : tmpList) {
            if (player == j) {
                drawCard2Position(card, (tmpList.indexOf(j)==2 ? 3: tmpList.indexOf(j)), table.getHeight(), table.getWidth());
                tableStich.put(tmpList.indexOf(j), card);
                break;
            }
        }
        currentCardsOnTable++;
        updateTable();
    }

    private void removeOuvertCard(BaseCard card) {
        if(ouvert && ouvertCards!=null){
            Optional<BaseCard> optCard = ouvertCards.stream()
                    .filter(c->c.suit.equals(card.suit)&&c.kind.equals(card.kind)).findFirst();
            optCard.ifPresent(baseCard -> {
                ouvertLabelMap.get(baseCard).setVisible(false);
                ouvertPanel.revalidate();
                ouvertPanel.repaint();
            });
        }
    }

    @Override
    protected void handleCards(Message message) {
        selectedGame = MessageGameSelected.GAMES.Ramsch;
        controlPanel.setVisible(true);
        JsonArray array = message.getParams().getAsJsonArray("cards");
        hand = new ArrayList<>();
        array.forEach(card -> {
            BaseCard c = new BaseCard(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            hand.add(c);
        });
        super.handleCards(message);
    }

    private String createUserLabelString(String msg, String player, boolean append2Name) {
        StringBuilder s = new StringBuilder();
        String color = "white";
        try {
            if (aufspieler > -1 && player.equals(players.get(aufspieler))) {
                color = "red";
            }
        } catch (Exception ex) {
            log.error("Aufspieler: " + aufspieler);
        }
        if (append2Name) {
            s.append(player);
        }
        if (msg.length() > 0) {
            s.append("<br>hat Stich(e)");
        }
        return addColor(s.toString(), color);
    }

    private static String addColor(String s, String color) {
        return "<html><font color=\"" +
                color +
                "\">" +
                s +
                "</font></html>";
    }

    @Override
    protected void createUI(int state, int posX, int posY, Dimension size) {
        super.createUI(state, posX, posY, size);
        setGameSpecificButtons(null);
        ouvertPanel = new JPanel(new GridLayout(1,10));
        userLabels[1].setOpaque(true);
        userLabels[1].setVisible(false);
        controlPanel.setVisible(true);


    }

    @Override
    protected void createUIConfigPanel() {
        super.createUIConfigPanel();
        JButton b = new JButton("selectTest");
        JButton b1 = new JButton("enddialog");
        configPanel.add(b);
        configPanel.add(b1);
        b.addActionListener(e -> {
            handleSelectGame();
        });
        b1.addActionListener(e -> {
            handleGameEnd(new MessageGameEnd("","","","","",120));
        });
    }

    private void createOuvertPanel(List<BaseCard> cards){
        hudTop.removeAll();
        ouvertPanel.removeAll();
        ouvertLabelMap = new HashMap<>();
        ouvertCardMap = new HashMap<>();
        cards = SortHand.sort(cards, selectedGame);
        setComponentSizes(ouvertPanel, new Dimension(mainFrame.getWidth(),hudTop.getHeight()));
        cards.forEach(this::getCardLabel4Ouvert);
        hudTop.add(ouvertPanel);
    }

    private void getCardLabel4Ouvert(BaseCard card){
        JLabel label = new JLabel();
        label.setIcon(cardIcons.get(card.suit +card.kind));
        ouvertLabelMap.put(card,label);
        ouvertCardMap.put(label,card);
        ouvertPanel.add(label);
    }



}
