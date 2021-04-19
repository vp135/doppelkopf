import base.BaseCard;
import base.messages.*;
import base.skat.Card;
import base.skat.SortHand;
import base.skat.messages.*;
import com.google.gson.JsonArray;
import jdk.tools.jlink.internal.TaskHelper;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SkatClient extends BaseClient implements IInputputHandler {


    private JButton sortKaro;
    private JButton sortHerz;
    private JButton sortPik;
    private JButton sortKreuz;
    private JButton sortNull;
    private JButton sortGrand;
    private GameSelected.GAMES selectedGame;
    private boolean handSpiel;
    private boolean ouvert;
    private int spectator;
    private int currentCardsOnTable;
    private int aufspieler;
    private JButton nextValue;
    private JButton pass;

    private BaseCard skat1;
    private BaseCard skat2;
    private boolean exchange;
    private JLabel cLabel1;
    private JLabel cLabel2;
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
    private JPanel ouvertPanel;


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
            createCardButtons(hand = SortHand.sortKaro(hand));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
        });
        sortNull.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortNull(hand));
            deselectAllSortButtons();
            sortNull.setBackground(Color.GREEN);
        });
        sortGrand.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortGrand(hand));
            deselectAllSortButtons();
            sortGrand.setBackground(Color.GREEN);
        });
        pass.addActionListener(e -> {
            handler.queueOutMessage(new Passen(c.name));
            nextValue.setVisible(false);
            pass.setVisible(false);
        });

        nextValue.setVisible(false);
        pass.setVisible(false);
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
            if (selectCards) {
                cards2Send.clear();
                cardLabels2Send.clear();
            }
        }
    }

    @Override
    protected void setCardClickAdapter() {
        cardClickAdapter = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                BaseCard card = cardMap.get(label);
                if (exchange) {
                    if (skat1 == null) {
                        skat1 = card;
                        cLabel1.setIcon(cardIcons.get(card.farbe + card.value));
                        cLabel1.setVisible(true);
                        hand.remove(card);
                        createCardButtons(hand);
                        setAnsageButtonState();
                    } else if (skat2 == null) {
                        skat2 = card;
                        cLabel2.setIcon(cardIcons.get(card.farbe + card.value));
                        cLabel2.setVisible(true);
                        hand.remove(card);
                        createCardButtons(hand);
                        setAnsageButtonState();
                    }
                } else if (wait4Player) {
                    wait4Player = false;
                    hand.remove(card);
                    label.setVisible(false);
                    handler.queueOutMessage(new PutCard(players.indexOf(c.name), card.farbe, card.value));
                    serverMessageLabel.setText("");
                    if (c.redrawCards) {
                        createCardButtons(hand);
                    }
                }
            }
        };
    }

    private void setAnsageButtonState() {
        button_ok.setEnabled((skat1 != null && skat2 != null));
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
                case Wait4Player.COMMAND:
                    handleWait4Player(message);
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
                case Skat.COMMAND:
                    handleSkat(message);
                    break;
                case RamschSkat.COMMAND:
                    handleRamschSkat();
                    break;
                case OuvertCards.COMMAND:
                    handleOuvertCards(message);
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void handleOuvertCards(RequestObject message) {
        JsonArray array = message.getParams().getAsJsonArray("cards");
        ouvertCards = new ArrayList<>();
        array.forEach(card -> {
            Card c = new Card(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            ouvertCards.add(c);
        });
        createOuvertPanel(ouvertCards);
    }

    private void handleGameSelected(RequestObject message) {
        selectedGame = GameSelected.GAMES.valueOf(message.getParams().get("game").getAsString());
        ouvert = message.getParams().get("ouvert").getAsBoolean();
    }

    private void handleSkat(RequestObject message) {
        middlePanel.removeAll();
        JsonArray array = message.getParams().getAsJsonArray("cards");
        skat1 = new Card(array.get(0).getAsString().split(" ")[1], array.get(0).getAsString().split(" ")[0]);
        skat2 = new Card(array.get(1).getAsString().split(" ")[1], array.get(1).getAsString().split(" ")[0]);

        cLabel1 = new JLabel(cardIcons.get(skat1.farbe + skat1.value));
        cLabel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moveSkatCard2Hand(skat1);
                skat1 = null;
                cLabel1.setVisible(false);
                setAnsageButtonState();
            }
        });
        cLabel2 = new JLabel(cardIcons.get(skat2.farbe + skat2.value));
        cLabel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moveSkatCard2Hand(skat2);
                skat2 = null;
                cLabel2.setVisible(false);
                setAnsageButtonState();
            }
        });
        middlePanel.add(buttonsPanel);
        button_ok.setVisible(true);
        middlePanel.add(cLabel1);
        middlePanel.add(cLabel2);

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
            handler.queueOutMessage(new Skat(c.name, Arrays.asList(skat1, skat2)));
            middlePanel.removeAll();
            middlePanel.repaint();
            exchange = false;
        });


        button_skat.addActionListener(e -> {
            middlePanel.remove(button_schieben);
            exchange = true;
            button_ok.setVisible(true);
            handler.queueOutMessage(new GetSkat(c.name));
        });

        button_schieben.addActionListener(e -> {
            handler.queueOutMessage(new Schieben());
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
            handler.queueOutMessage(new GameSelected(c.name, selectedGame, handSpiel, ouvert));
            if (!handSpiel) {
                handler.queueOutMessage(new Skat(c.name, Arrays.asList(skat1, skat2)));
            }
            middlePanel.removeAll();
            middlePanel.repaint();
            exchange = false;
        });

        buttonsPanel.add(button_ok);

        button_skat = new JButton("Skat aufnehmen");
        button_skat.addActionListener(e -> {
            handler.queueOutMessage(new GetSkat(c.name));
            exchange = true;
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
            button_ok.setEnabled(true);
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

    private void moveSkatCard2Hand(BaseCard card) {
        hand.add(card);
        getCardLabel4Hand(card);
    }

    private void handleReizen(RequestObject message) {
        int val = message.getParams().get("value").getAsInt();
        boolean active = message.getParams().get("active").getAsBoolean();
        if (active) {
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
            //gameMessageLabel.setText(String.valueOf(message.getParams().get("value").getAsInt()));
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
        Reizen reizen = new Reizen(c.name, Integer.parseInt(nextValue.getText()), true);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    ActionListener hoeren = e -> {
        Reizen reizen = new Reizen(c.name, 0, false);
        handler.queueOutMessage(reizen);
        nextValue.setVisible(false);
        pass.setVisible(false);
    };

    private void handleAnnounceSpectator(RequestObject message) {
        spectator = message.getParams().get("player").getAsInt();
        aufspieler = message.getParams().get("starter").getAsInt();
        if (players.size() > 3 && players.get(spectator).equals(c.name)) {
            handler.queueOutMessage(new DisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    private void handleUserPanelUpdate(RequestObject message) {
        int ownNumber = players.indexOf(c.name);
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
            case 1: {
                userLabel_1.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 2: {
                userLabel_3.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 3: {
                userLabel_2.setText(createUserLabelString(
                        message.getParams().get("text").getAsString(),
                        message.getParams().get("player").getAsString(),
                        true));
                break;
            }
            case 0: {
                if (message.getParams().get("player").getAsString().equals(c.name)) {
                    userLabel_4.setText(createUserLabelString(
                            message.getParams().get("text").getAsString(),
                            "Du",
                            false));
                } else {
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
        SkatEndDialog e = new SkatEndDialog(
                selectedGame,
                message.getParams().get("re1").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("player1").getAsString(),
                message.getParams().get("player2").getAsString(),
                message.getParams().get("player3").getAsString(),
                message.getParams().get("remain").getAsInt());
        e.showDialog(this.mainFrame);
        clearPlayArea();
        selectCards = false;
        wait4Player = false;
        hand = new ArrayList<>();
        handler.queueOutMessage(new ReadyForNextRound(players.indexOf(c.name)));
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

    private void handlePutCard(RequestObject message) {
        int ownNumber = players.indexOf(c.name);
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
            }
            updateTable();
            currentCardsOnTable = 0;
            tableStich.clear();
        }

        int player = message.getParams().get("player").getAsInt();
        Card card = new Card(
                message.getParams().get("wert").getAsString(),
                message.getParams().get("farbe").getAsString());

        if(ouvert && ouvertCards!=null){
            Optional<BaseCard> optCard = ouvertCards.stream()
                    .filter(c->c.farbe.equals(card.farbe)&&c.value.equals(card.value)).findFirst();
            optCard.ifPresent(baseCard -> {
                ouvertCards.remove(baseCard);
                layeredPane.remove(ouvertPanel);
                createOuvertPanel(ouvertCards);
            });
        }

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

    @Override
    protected void handleCards(RequestObject message) {
        selectedGame = GameSelected.GAMES.Ramsch;
        JsonArray array = message.getParams().getAsJsonArray("cards");
        hand = new ArrayList<>();
        array.forEach(card -> {
            Card c = new Card(card.getAsString().split(" ")[1],
                    card.getAsString().split(" ")[0]);
            hand.add(c);
        });
        super.handleCards(message);
        //createOuvertPanel(hand);
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
    protected void createUI(int state, int posX, int posY, Dimension size, boolean test) {
        super.createUI(state, posX, posY, size, test);
        setGameSpecificButtons(null);
        controlPanel.setVisible(true);
    }


    private void createOuvertPanel(List<BaseCard> cards){
        ouvertPanel = new JPanel(new GridLayout(1,10));
        userLabel_2.setOpaque(true);
        userLabel_2.setVisible(false);
        createCardButtons(cards,ouvertPanel);
        layeredPane.add(ouvertPanel,3);
        layeredPane.revalidate();
        layeredPane.repaint();
        setComponentSizes(ouvertPanel, new Dimension(mainFrame.getSize()));
    }


}
