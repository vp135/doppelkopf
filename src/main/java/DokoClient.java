import base.Statics;
import base.communication.ComClient;
import base.config.Configuration;
import base.game.Card;
import base.game.doko.DokoCards;
import base.game.doko.DokoEndDialog;
import base.game.doko.SortHand;
import base.game.doko.assist.Assist;
import base.interfaces.IDialogInterface;
import base.interfaces.IInputputHandler;
import base.messages.*;
import base.messages.admin.MessageAcknowledge;
import base.messages.doko.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static base.messages.doko.MessageGameSelected.GAMES.*;

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
    private DokoEndDialog endDialog;
    private Card mustPlay = null;
    private Assist assist;


    public DokoClient(ComClient handler, List<String> players, Configuration c) {
        super(handler, players, c);
        currentGame = Statics.game.DOKO;
    }

    @Override
    public void createGameSpecificButtons() {
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


        sortNormal.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortNormal(hand, schweinExists));
            deselectAllSortButtons();
            sortNormal.setBackground(Color.GREEN);
            selectedGame = NORMAL;
            setTrumpfCards();
        });
        sortDamen.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortDamenSolo(hand));
            deselectAllSortButtons();
            sortDamen.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.DAMEN;
            setTrumpfCards();
        });
        sortBuben.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortBubenSolo(hand));
            deselectAllSortButtons();
            sortBuben.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.BUBEN;
            setTrumpfCards();
        });
        sortBubenDamen.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortBubenDamenSolo(hand));
            deselectAllSortButtons();
            sortBubenDamen.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.BUBENDAMEN;
            setTrumpfCards();
        });
        sortFleischlos.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortFleischlos(hand));
            deselectAllSortButtons();
            sortFleischlos.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.FLEISCHLOS;
            setTrumpfCards();
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.KREUZ;
            setTrumpfCards();
        });
        sortPik.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.PIK;
            setTrumpfCards();
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.HERZ;
            setTrumpfCards();
        });
        sortKaro.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
            selectedGame = KARO;
            setTrumpfCards();
        });
        sortArmut.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortArmut(hand, schweinExists));
            deselectAllSortButtons();
            sortArmut.setBackground(Color.GREEN);
            selectedGame = ARMUT;
            setTrumpfCards();
        });
        sortKoenige.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKoenige.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.KOENIGE;
            setTrumpfCards();
        });
        sortHochzeit.addActionListener(e -> {
            createCardButtons(hand = SortHand.sortNormal(hand, schweinExists));
            deselectAllSortButtons();
            sortHochzeit.setBackground(Color.GREEN);
            selectedGame = MessageGameSelected.GAMES.HOCHZEIT;
            setTrumpfCards();
        });
    }


    private void setTrumpfCards() {
        if (hand != null) {
            hand.forEach(card -> card.trump = DokoCards.isTrumpf(card, selectedGame));
        }
        new Assist(hand, selectedGame);
    }


    @Override
    protected void setGameSpecificButtons(List<Card> cards) {

        controlPanel.removeAll();
        JButton vorbehalt = new JButton("OK");
        vorbehalt.addActionListener(e -> {
            handler.queueOutMessage(new MessageGameSelected(players.indexOf(c.connection.name), selectedGame));
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

        if (cards.stream().filter(p -> p.trump).count() < 4) {
            controlPanel.add(sortArmut);
        }
        if (cards.stream().filter(p -> p.kind.equals(Statics.KOENIG)).count() > 4) {
            controlPanel.add(sortKoenige);
        }
        if (cards.stream().filter(p -> p.kind.equals(Statics.DAME) && p.suit.equals(Statics.KREUZ)).count() > 1) {
            controlPanel.add(sortHochzeit);
        }

        controlPanel.add(vorbehalt);
        Dimension d = new Dimension(mainFrame.getSize().width, mainFrame.getSize().height / (30));
        setComponentSizes(controlPanel, d);
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
                    if (hand.size() == 10) {
                        setSendCardButton(MessageSendCards.POOR, "Karten zurueckgeben");
                    } else {
                        controlPanel.removeAll();
                    }
                } else {
                    if (wait4Player) {
                        if (c.other.bedienen) {
                            boolean allowed = false;
                            if (mustPlay != null) {
                                if (mustPlay.trump) {
                                    if (card.trump || assist.playerBucket.trumpf.size() < 1) {
                                        allowed = true;
                                    }
                                } else {
                                    if (card.suit.equals(mustPlay.suit) || assist.playerBucket.getListBySuit(mustPlay.suit).size() < 1) {
                                        allowed = true;
                                    }
                                }
                            } else {
                                allowed = true;
                            }
                            if (allowed) {
                                wait4Player = false;
                                hand.remove(card);
                                label.setVisible(false);
                                handler.queueOutMessage(new MessagePutCard(players.indexOf(c.connection.name), card));
                                if (c.ui.redrawCards) {
                                    createCardButtons(hand);
                                } else {
                                    System.out.println("Nicht bedient");
                                }
                            }
                        } else {
                            wait4Player = false;
                            hand.remove(card);
                            label.setVisible(false);
                            handler.queueOutMessage(new MessagePutCard(players.indexOf(c.connection.name), card));
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
                for (int i = 0; i < exchangeCards.length; i++) {
                    if (cLabels[i] != null && cLabels[i] == label) {
                        card = exchangeCards[i];
                        break;
                    }
                }
                moveCard2Hand(card);
                if (hand.size() == 10) {
                    setSendCardButton(MessageSendCards.POOR, "Karten zurueckgeben");
                } else {
                    controlPanel.removeAll();
                }
                for (int i = 0; i < exchangeCards.length; i++) {
                    if (exchangeCards[i] == card) {
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
    public void handleInput(Message message) {
        try {
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
                case MessageCurrentStich.LAST: {
                }
                case MessageCurrentStich.SPECIFIC: {
                    if (letzterStich == null) {
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
                case MessageGameSelected.COMMAND: {
                    handleGameSelected(message);
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
                case MessagePlayerList.IN_LOBBY: {
                    handlePlayersInLobby(message);
                    break;
                }
                case MessageAcknowledge.COMMAND: {
                    endDialog.ackowledge();
                }
                default:
                    break;
            }
        } catch (Exception ex) {
            log.error(message + " => " + ex);
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

    private void handlePutCard(Message message) {
        try {
            middlePanel.setBackground(new Color(0, 0, 0, 0));
            middlePanel.setOpaque(false);
            int ownNumber = players.indexOf(c.connection.name);
            List<Integer> tmpList = new ArrayList<>();
            int i = ownNumber;
            //log.info("own:" +i);

            while (tmpList.size() < 4) {
                if (i != spectator) {
                    tmpList.add(i);
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
                tableStich.clear();
            }

            MessagePutCard messagePutCard = new MessagePutCard(message);
            //log.info("player:" + i);
            Card card = messagePutCard.getCard(Statics.game.DOKO);
            card.trump = DokoCards.isTrumpf(card, selectedGame);
            currentCardsOnTable++;
            if (currentCardsOnTable == 1) {
                mustPlay = card;
            } else if (currentCardsOnTable > 3) {
                mustPlay = null;
            }

            for (int j : tmpList) {
                if (messagePutCard.getPlayerNumber() == j) {
                    //log.info(tmpList.indexOf(j) + ":" + card);
                    drawCard2Position(card, tmpList.indexOf(j), table.getHeight(), table.getWidth());
                    tableStich.put(tmpList.indexOf(j), card);
                    break;
                }
            }
            updateTable();
            if (assist != null) {
                assist.putCard(messagePutCard.getCard(Statics.game.DOKO));
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }

    private void handleAnnounceSpectator(Message message) {
        MessageAnnounceSpectator messageAnnounceSpectator = new MessageAnnounceSpectator(message);
        spectator = messageAnnounceSpectator.getSpectatorNumber();
        aufspieler = messageAnnounceSpectator.getStarterNumber();
        if (players.size() > 4 && players.get(spectator).equals(c.connection.name)) {
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
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        MessageSendCards messageSendCards = new MessageSendCards(message);
        boolean isRich = messageSendCards.getReceiver().equals(MessageSendCards.RICH);
        middlePanel.removeAll();
        selectCards = isRich;
        exchangeCards = messageSendCards.getCards().toArray(new Card[0]);

        cLabels = new JLabel[exchangeCards.length];
        for (int i = 0; i < exchangeCards.length; i++) {
            cLabels[i] = new JLabel(cardIcons.get(exchangeCards[i].toTrimedString()));
            if (isRich) {
                cLabels[i].addMouseListener(exchangeCardClickAdapter);
            }
            middlePanel.add(cLabels[i]);
        }

        if (isRich) {
            setSendCardButton(MessageSendCards.POOR, "Karten zurueckgeben");
        } else {
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

    private void handleGameSelected(Message message) {
        MessageGameSelected messageGameSelected = new MessageGameSelected(message);
        selectedGame = messageGameSelected.getSelectedGame();
        if (hand != null && hand.size() > 0) {
            hand = SortHand.sort(hand, selectedGame, schweinExists);
            createCardButtons(hand);
            if (selectedGame == NORMAL
                    || selectedGame == KARO
                    || selectedGame == ARMUT) {
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
        if (assist != null) {
            assist.close();
        }
        assist = new Assist(hand, selectedGame);
    }

    private void handleGameEnd(Message message) {
        updateTable();
        endDialog = new MessageGameEnd(message).getEndDialog(this);
        endDialog.showDialog(this.mainFrame);

    }

    @Override
    public void quitEnd() {
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
        hand = messageCards.getCards(Statics.game.DOKO);
        super.handleCards(message);

        new Assist(hand, NORMAL);
        new Assist(hand, DAMEN);
        new Assist(hand, BUBEN);
        new Assist(hand, BUBENDAMEN);
        new Assist(hand, FLEISCHLOS);
        new Assist(hand, KREUZ);
        new Assist(hand, PIK);
        new Assist(hand, HERZ);
        new Assist(hand, KARO);
    }

    private void handleLastStich(Message message) {
        handleLastStich(message, 4, spectator);
    }

    private void handleUserPanelUpdate(Message message) {
        MessageUpdateUserPanel messageUpdateUserPanel = new MessageUpdateUserPanel(message);
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

        int otherNumber = players.indexOf(messageUpdateUserPanel.getPlayerName());

        switch (tmpList.indexOf(otherNumber)) {
            case 1:
            case 2:
            case 3:
                userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                        messageUpdateUserPanel.getText(),
                        messageUpdateUserPanel.getPlayerName(),
                        true));
                break;
            case 0: {
                if (messageUpdateUserPanel.getPlayerName().equals(c.connection.name)) {
                    userLabels[tmpList.indexOf(otherNumber)].setText(createUserLabelString(
                            messageUpdateUserPanel.getText(),
                            "Du",
                            false));
                } else {
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
        middlePanel.setBackground(Color.BLACK);
        middlePanel.setOpaque(true);
        controlPanel.removeAll();
        autoSelectArmutCards();
        setSendCardButton(MessageSendCards.RICH, "Armut anbieten");
    }

    private void setSendCardButton(String receiver, String buttonText) {
        controlPanel.removeAll();
        JButton sendCardsButton = new JButton(buttonText);
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
        List<Card> cards = hand.stream().filter(card -> card.trump).collect(Collectors.toList());
        exchangeCards = new Card[cards.size()];
        cLabels = new JLabel[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            cLabels[i] = new JLabel();
        }
        cards.forEach(c -> moveCard2Exchange(c, true));
        middlePanel.setVisible(true);
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
    protected void moveCard2Hand(Card card) {
        hand.add(card);
        hand = SortHand.sort(hand, selectedGame, true);
        createCardButtons(hand);

    }
}
