import base.*;
import base.messages.*;
import base.skat.Card;
import base.skat.SortHand;
import base.skat.messages.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class SkatClient extends BaseClient implements IInputputHandler, IDialogInterface {

    //UI
    private JButton sortKaro;
    private JButton sortHerz;
    private JButton sortPik;
    private JButton sortKreuz;
    private JButton sortNull;
    private JButton sortGrand;

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
    private List<Card> ouvertCards;
    private Map<Card,JLabel> ouvertLabelMap;
    private JPanel ouvertPanel;


    //Spielvariablen
    private boolean handSpiel;
    private GameSelected.GAMES selectedGame;
    private boolean ouvert;
    private JButton nextValue;
    private JButton pass;
    private boolean selectGame;
    private SkatEndDialog endDialog;
    private GameSelected.GAMES sortGame = null;


    public SkatClient(ComClient handler, List<String> players, Configuration c) {
        super(handler, players, c);
        currentGame = Statics.game.SKAT;
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
            sortGame = GameSelected.GAMES.Karo;
            sortKaro.setBackground(Color.GREEN);
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortGame = GameSelected.GAMES.Herz;
            sortHerz.setBackground(Color.GREEN);
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortGame = GameSelected.GAMES.Pik;
            sortPik.setBackground(Color.GREEN);
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortGame = GameSelected.GAMES.Kreuz;
            sortKreuz.setBackground(Color.GREEN);
        });
        sortNull.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortNull(hand));
            deselectAllSortButtons();
            sortGame = GameSelected.GAMES.Null;
            sortNull.setBackground(Color.GREEN);
        });
        sortGrand.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortGrand(hand));
            deselectAllSortButtons();
            sortGame = GameSelected.GAMES.Grand;
            sortGrand.setBackground(Color.GREEN);
        });
        pass.addActionListener(e -> {
            handler.queueOutMessage(new Passen(c.connection.name));
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
        handCardClickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                Card card = cardMap.get(label);
                if (selectCards) {
                    moveCard2Exchange(card);
                    setAnsageButtonState();
                } else if (wait4Player) {
                    wait4Player = false;
                    hand.remove(card);
                    label.setVisible(false);
                    handler.queueOutMessage(new PutCard(players.indexOf(c.connection.name), card));
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
                Card card = null;
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
            if(selectedGame== GameSelected.GAMES.Ramsch){
                button_ok.setEnabled(!selectGame);
            }
            else{
                button_ok.setEnabled(true);
            }
        }
    }

    @Override
    public void handleInput(RequestObject message) {
        super.handleInput(message);
        try {
            switch (message.getCommand()) {
                case Cards.COMMAND:
                    deselectAllSortButtons();
                    handleCards(message);
                    break;
                case PutCard.COMMAND:
                    handlePutCard(message);
                    break;
                case CurrentStich.LAST:
                case CurrentStich.SPECIFIC:
                    if (letzterStich == null) {
                        handleLastStich(message);
                    }
                    break;
                case GameEnd.COMMAND:
                    handleGameEnd(message);
                    break;
                case UpdateUserPanel.COMMAND:
                    handleUserPanelUpdate(message);
                    break;
                case AnnounceSpectator.COMMAND:
                    handleAnnounceSpectator(message);
                    break;
                case Reizen.COMMAND:
                    handleReizen(message);
                    break;
                case SelectGame.COMMAND:
                    handleSelectGame();
                    break;
                case GameSelected.COMMAND:
                    handleGameSelected(message);
                    break;
                case RamschSkat.COMMAND:
                    handleRamschSkat();
                    break;
                case Skat.COMMAND:
                    handleSkat(message);
                    break;
                case OuvertCards.COMMAND:
                    handleOuvertCards(message);
                    break;
                case GrandHand.COMMAND:
                    handleGrandHand(message);
                    break;
                case Acknowledge.COMMAND:
                    endDialog.ackowledge();
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void handleGrandHand(RequestObject message) {
        gameMessageLabel.setText("Willst du einen Grand hand spielen?");
        middlePanel.removeAll();
        JButton gh = new JButton("Grand hand");
        JButton ramsch = new JButton("Ramsch");

        gh.addActionListener(e ->{
            handler.queueOutMessage(new GrandHand(c.connection.name,true));
            middlePanel.removeAll();
            middlePanel.revalidate();
            middlePanel.repaint();
            gameMessageLabel.setText("");
        });
        ramsch.addActionListener(e ->{
            handler.queueOutMessage(new GrandHand(c.connection.name,false));
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

    private void handleOuvertCards(RequestObject message) {
        ouvertCards = readCards(message);
        createOuvertPanel(ouvertCards);
    }

    private void handleGameSelected(RequestObject message) {
        selectedGame = GameSelected.GAMES.valueOf(message.getParams().get("game").getAsString());
        createCardButtons(hand = SortHand.sort(hand,selectedGame));
        controlPanel.setVisible(false);
        ouvert = message.getParams().get("ouvert").getAsBoolean();
    }

    private void handleSkat(RequestObject message) {
        middlePanel.removeAll();
        selectCards = true;
        exchangeCards = readCards(message).toArray(new Card[0]);
        middlePanel.add(buttonsPanel);
        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0;i<exchangeCards.length;i++){
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            cLabels[i].setBackground(Color.BLACK);
            cLabels[i].addMouseListener(exchangeCardClickAdapter);
            //midllePanel.setOpaque(false);
            //middlePanel.setBackground(Color.Black);
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
            handler.queueOutMessage(new Skat(c.connection.name, Arrays.asList(exchangeCards)));
            middlePanel.removeAll();
            middlePanel.repaint();
            selectCards = false;
        });

        button_skat.addActionListener(e -> {
            middlePanel.remove(button_schieben);
            selectCards = true;
            button_ok.setVisible(true);
            handler.queueOutMessage(new GetSkat(c.connection.name));
        });

        button_schieben.addActionListener(e -> {
            handler.queueOutMessage(new Schieben(c.connection.name));
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
        buttonsPanel = new JPanel(new GridLayout(5, 2));
        button_karo = new JButton("Karo");
        button_herz = new JButton("Herz");
        button_pik = new JButton("Pik");
        button_kreuz = new JButton("Kreuz");
        button_null = new JButton("Null");
        button_grand = new JButton("Grand");
        button_ouvert = new JButton("Ouvert");

        button_karo.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Karo;
            deselectGameButtons();
            setOuvertButtonState();
            button_karo.setBackground(Color.GREEN);
        });
        button_herz.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Herz;
            deselectGameButtons();
            setOuvertButtonState();
            button_herz.setBackground(Color.GREEN);
        });
        button_pik.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Pik;
            deselectGameButtons();
            setOuvertButtonState();
            button_pik.setBackground(Color.GREEN);
        });
        button_kreuz.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Kreuz;
            deselectGameButtons();
            setOuvertButtonState();
            button_kreuz.setBackground(Color.GREEN);
        });
        button_null.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Null;
            deselectGameButtons();
            setOuvertButtonState();
            button_null.setBackground(Color.GREEN);
        });
        button_grand.addActionListener(e -> {
            selectedGame = GameSelected.GAMES.Grand;
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
            handler.queueOutMessage(new GameSelected(c.connection.name, selectedGame, handSpiel, ouvert));
            if (!handSpiel) {
                handler.queueOutMessage(new Skat(c.connection.name, Arrays.asList(exchangeCards)));
            }
            middlePanel.removeAll();
            middlePanel.repaint();
            overLayer.moveToFront(configPanel);
            layeredPane.setLayer(table,0);
            table.setBackground(new Color(0,0,0,0));
            layeredPane.setLayer(hud,1);
            hud.setBackground(new Color(0,0,0,0));

            selectCards = false;
            selectGame = false;
        });

        buttonsPanel.add(button_ok);
        button_skat = new JButton("Skat aufnehmen");
        button_skat.addActionListener(e -> {
            handler.queueOutMessage(new GetSkat(c.connection.name));
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
        if(selectedGame!= GameSelected.GAMES.Ramsch){
            setAnsageButtonState();
        }
    }

    private void setOuvertButtonState() {
        if (!handSpiel) {
            if (selectedGame != GameSelected.GAMES.Null) {
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


    private void handleReizen(RequestObject message) {
        int val = message.getParams().get("value").getAsInt();
        if (message.getParams().get("active").getAsBoolean()) {
            int nextVal = 0;
            if (val == 0) {
                nextVal = 18;
            } else {
                for (int i = 0; i < Reizen.VALUES.length; i++) {
                    if (Reizen.VALUES[i] == val) {
                        nextVal = Reizen.VALUES[i + 1];
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
        Reizen reizen = new Reizen(c.connection.name, Integer.parseInt(nextValue.getText()), true);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    ActionListener hoeren = e -> {
        Reizen reizen = new Reizen(c.connection.name, 0, false);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    private void handleAnnounceSpectator(RequestObject message) {
        spectator = message.getParams().get("player").getAsInt();
        aufspieler = message.getParams().get("starter").getAsInt();
        if (players.size() > 3 && players.get(spectator).equals(c.connection.name)) {
            handler.queueOutMessage(new DisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    private void handleUserPanelUpdate(RequestObject message) {
        String playerName = message.getParams().get("player").getAsString();
        String text = message.getParams().get("text").getAsString();
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

        int otherNumber = players.indexOf(playerName);

        switch (tmpList.indexOf(otherNumber)) {
            case 1:
                userLabels[1].setText(createUserLabelString(
                        text,
                        playerName,
                        true));
                break;
            case 2:
                userLabels[3].setText(createUserLabelString(
                        text,
                        playerName,
                        true));
                break;
            case 3:
                break;
            case 0: {
                if (playerName.equals(c.connection.name)) {
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            text,
                            "Du",
                            false));
                }
                else {
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


    private void handleGameEnd(RequestObject message) {
        updateTable();
        ouvertCards = new ArrayList<>();
        createOuvertPanel(ouvertCards);
        endDialog = new SkatEndDialog(this, selectedGame,
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
        handler.queueOutMessage(new ReadyForNextRound(players.indexOf(c.connection.name)));
    }


    private void handleLastStich(RequestObject message) {
        handleLastStich(message,3,spectator);
    }


    private void handlePutCard(RequestObject message) {
        middlePanel.setBackground(new Color(0,0,0,0));
        middlePanel.setOpaque(false);
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

        int playerNumber = message.getParams().get("player").getAsInt();
        Card card = new Card(message.getParams().get("wert").getAsString(),
                message.getParams().get("farbe").getAsString());

        removeOuvertCard(card);

        for (int j : tmpList) {
            if (playerNumber == j) {
                drawCard2Position(card, (tmpList.indexOf(j)==2 ? 3: tmpList.indexOf(j)), table.getHeight(), table.getWidth());
                tableStich.put(tmpList.indexOf(j), card);
                break;
            }
        }
        currentCardsOnTable++;
        updateTable();
    }

    private void removeOuvertCard(Card card) {
        if(ouvert && ouvertCards!=null){
            Optional<Card> optCard = ouvertCards.stream()
                    .filter(c->c.farbe.equals(card.farbe)&&c.value.equals(card.value)).findFirst();
            optCard.ifPresent(baseCard -> {
                ouvertLabelMap.get(baseCard).setVisible(false);
                ouvertPanel.revalidate();
                ouvertPanel.repaint();
            });
        }
    }

    @Override
    protected void handleCards(RequestObject message) {
        selectedGame = GameSelected.GAMES.Ramsch;
        controlPanel.setVisible(true);
        hand = readCards(message);
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        super.handleCards(message);
    }

    private List<Card> readCards(RequestObject message) {
        List<Card> cards = new ArrayList<>();
        message.getParams().getAsJsonArray("cards").forEach(card -> {
            String[] parts = card.getAsString().split(" ", 2);
            cards.add(new Card(parts[1], parts[0]));
        });
        return cards;
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
            handleGameEnd(new GameEnd("","","","","",120));
        });
    }

    private void createOuvertPanel(List<Card> cards){
        hudTop.removeAll();
        ouvertPanel.removeAll();
        ouvertLabelMap = new HashMap<>();
        cards = SortHand.sort(cards, selectedGame);
        setComponentSizes(ouvertPanel, new Dimension(mainFrame.getWidth(),hudTop.getHeight()));
        cards.forEach(this::getCardLabel4Ouvert);
        hudTop.add(ouvertPanel);
        ouvertPanel.revalidate();
        ouvertPanel.repaint();
    }

    private void getCardLabel4Ouvert(Card card){
        JLabel label = new JLabel();
        label.setIcon(cardIcons.get(card.toTrimedString()));
        ouvertLabelMap.put(card,label);
        ouvertPanel.add(label);
    }



}
