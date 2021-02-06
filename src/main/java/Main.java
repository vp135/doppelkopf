import base.*;
import base.messages.MessageAllPlayers;
import com.google.gson.JsonArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private final Logger log = new Logger(this.getClass().getName());



    Socket socket;
    static Main m;
    JPanel panel;
    JPanel mainPanel;
    JPanel table;
    JPanel topPanel;
    JLabel topLabel_1;
    JLabel topLabel_2;
    JFrame mainFrame;

    private String selectedGame = GameSelected.NORMAL;
    private List<Card> hand;
    private String name;
    private JPanel controlPanel;
    private List<String> players = new ArrayList<>();
    private final JList<String> playerList = new JList<>();
    private JLabel cardPos1;
    private JLabel cardPos2;
    private JLabel cardPos3;
    private JLabel cardPos4;
    private final JTextArea userLabel_1 = new JTextArea();
    private final JTextArea userLabel_2 = new JTextArea();
    private final JTextArea userLabel_3 = new JTextArea();
    private final JTextArea userLabel_4 = new JTextArea();
    private JButton sortNormal;
    private JButton hochzeit;
    private JButton sortBuben;
    private JButton sortDamen;
    private JButton sortBubenDamen;
    private JButton sortFleisch;
    private JButton sortKreuz;
    private JButton sortPik;
    private JButton sortHerz;
    private JButton sortKaro;
    private JButton sortArmut;
    private JButton koenige;
    private ArrayList<Card> cards2Send;
    private List<JLabel> cardLabels2Send;
    private Dimension frameSize = new Dimension(100,100);
    private DokoServer dokoServer;
    private JButton start;
    private JButton join;
    private JFrame createJoinFrame;
    private int port;
    private String hostname;
    private JFrame letzterStich;
    private boolean wait4Player = false;
    private boolean selectCards = false;
    private boolean schweinExists = false;
    private int spectator;
    private RequestObject lastMessage = null;

    private Map<Card,JLabel> labelMap;

    private final AutoResetEvent ev = new AutoResetEvent(true);
    private boolean isAdmin;
    private JFrame admin_panel;

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }


    public static void main(String[] args) {
        UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("Label.background",Color.BLACK);
        UIManager.put("Label.foreground",Color.WHITE);
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("TextField.background",new Color(70,70,70));
        UIManager.put("TextField.foreground",Color.WHITE);
        UIManager.put("Panel.background",Color.BLACK);
        UIManager.put("List.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("List.background",Color.BLACK);
        UIManager.put("List.foreground",Color.WHITE);
        UIManager.put("TextArea.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("TextArea.background",Color.BLACK);
        UIManager.put("TextArea.foreground",Color.WHITE);
        UIManager.put("OptionPane.messageForeground",Color.WHITE);


        m = new Main();
        m.createOrJoin();
    }

    public void createOrJoin() {
        log.info("starting Lobby UI");
        //new Thread(() -> {
        Configuration c = Configuration.fromFile();
        createJoinFrame = new JFrame();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JPanel inputs = new JPanel(new GridLayout(5, 2));
        panel.add(inputs);
        panel.add(playerList);
        inputs.add(new JLabel("Spielername"));
        JTextField playername = new JTextField();
        inputs.add(playername);
        inputs.add(new JLabel("Hostname"));
        JTextField hostname = new JTextField();
        inputs.add(hostname);
        inputs.add(new JLabel("Port"));
        JTextField port = new JTextField();
        inputs.add(port);
        JButton create = new JButton("server erstellen");
        join = new JButton("beitreten");
        start = new JButton("start");
        start.setEnabled(false);
        panel.add(playerList);
        inputs.add(create);
        inputs.add(join);

        if (c != null) {
            playername.setText(c.name);
            hostname.setText(c.server);
            port.setText(String.valueOf(c.port));
        }

        create.addActionListener(e -> {
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
                name = playername.getText();
                dokoServer = new DokoServer(Integer.parseInt(port.getText()));
                create.setEnabled(false);
                join.setEnabled(false);
                openTCPConnection("127.0.0.1", Integer.parseInt(port.getText()),false);
                sendingThread();
                SendByTCP(new AddPlayer(name));
                SendByTCP(new GetVersion(name,DokoServer.VERSION));
                inputs.add(start);
            } else if (!port.getText().trim().equals("")) {
                name = playername.getText();
                dokoServer = new DokoServer(Integer.parseInt(port.getText()));
                inputs.add(start);
            }
            new Configuration(name, hostname.getText(), Integer.parseInt(port.getText())).saveConfig();
        });

        join.addActionListener(e -> {
            this.hostname = hostname.getText();
            this.port = Integer.parseInt(port.getText());
            new Thread(() -> {
                name = playername.getText().trim();
                if (!name.equals("")) {
                    openTCPConnection(hostname.getText(), Integer.parseInt(port.getText()),false);
                    log.info("verbinde");
                    int dots = 0;
                    while (wait.get()) {
                        if (dots > 3) {
                            dots = 0;
                        }
                        StringBuilder text = new StringBuilder("verbinde");
                        for (int i = 0; i < dots; i++) {
                            text.append(".");
                        }
                        join.setText(text.toString());
                        try {
                            Thread.sleep(300);
                            dots++;
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        join.setText("verbunden");
                        log.info("verbunden");
                        new Configuration(name, hostname.getText(), Integer.parseInt(port.getText())).saveConfig();
                        create.setEnabled(false);
                        sendingThread();
                        SendByTCP(new GetVersion(name,DokoServer.VERSION));
                    } else {
                        join.setText("beitreten");
                        log.info("beitreten");
                    }
                }
            }).start();

        });
        start.addActionListener(e -> {
            isAdmin = true;
            createAdminUI();
            dokoServer.startGame();
        });
        createJoinFrame.pack();
        createJoinFrame.add(panel);
        createJoinFrame.setVisible(true);
        createJoinFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        createJoinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //}).start();
    }

    private void createAdminUI() {

        admin_panel = new JFrame("Admin Panel");
        JPanel adminMainPanel = new JPanel(new GridLayout(10,1));
        JButton button_abortGame = new JButton("Spiel abbrechen");
        JTextField stichNumber = new JTextField();
        JButton button_showStich = new JButton("Stich anzeigen");
        JButton button_reset = new JButton("alle Clients aktualisieren");

        adminMainPanel.add(button_abortGame);
        adminMainPanel.add(new JLabel(""));
        adminMainPanel.add(stichNumber);
        adminMainPanel.add(button_showStich);
        adminMainPanel.add(button_reset);
        admin_panel.add(adminMainPanel);
        admin_panel.pack();
        admin_panel.setVisible(true);

        button_abortGame.addActionListener(e -> SendByTCP(new AbortGame()));

        JButton button_selectGame = new JButton("Spiel auswählen");
        button_selectGame.addActionListener(e -> dokoServer.send2All(new SelectGame()));

        adminMainPanel.add(button_selectGame);

        JButton button_close = new JButton("close");
        button_close.addActionListener(e -> admin_panel.dispose());
    }

    AtomicBoolean wait = new AtomicBoolean(true);
    public void openTCPConnection(String hostname, int port, boolean reconnect) {
        wait.set(true);
        new Thread(() -> {
            if(!reconnect) {
                try {
                    socket = new Socket(hostname, port);
                    Listen(hostname, port);
                    log.info("Connected to Server");
                } catch (IOException e) {
                    log.info("Could not connect to Server: " + e);
                }
            }
            else{
                while (socket == null) {
                    try {
                        socket = new Socket(hostname, port);
                        Listen(hostname, port);
                        log.info("Connected to Server");
                    } catch (IOException e) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        log.info("Could not connect to Server: " + e);
                    }
                }
            }
            wait.set(false);
        }).start();

    }

    private void Listen(String hostname, int port) {
        new Thread(() -> {
            if (socket != null) {
                SendByTCP(new AddPlayer(name));
                String ServerReply;
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    log.error(ex.toString());
                }
                if (in != null) {
                    while (socket != null) {
                        try {
                            if ((ServerReply = in.readLine()) != null) {
                                if (ServerReply.length() > 0) {
                                    System.out.println("ServerReply: " + ServerReply);
                                    handleInput(ServerReply);
                                }
                            }
                        } catch (Exception ex) {
                            log.error("reconnect_shit: " + ex.toString());
                            socket = null;
                            openTCPConnection(hostname, port, true);
                        }
                    }
                }
            }
        }).start();
    }

    private void handleInput(String serverReply) {
        RequestObject message = RequestObject.fromString(serverReply);
        log.info("received: " +message.getCommand());
        switch (message.getCommand()) {
            case PlayersInLobby.COMMAND: {
                playerList.removeAll();
                players = new ArrayList<>();
                DefaultListModel<String> model = new DefaultListModel<>();
                message.getParams().get("players").getAsJsonArray().forEach(player -> {
                    model.addElement(player.getAsString());
                    players.add(player.getAsString());
                });
                playerList.setModel(model);
                if (players.size() > 3) {
                    start.setEnabled(true);
                }
                join.setEnabled(false);
                break;
            }
            case Cards.COMMAND: {
                selectedGame = GameSelected.NORMAL;
                JsonArray array = message.getParams().getAsJsonArray("cards");
                updateTable();
                panel.removeAll();
                hand = new ArrayList<>();
                array.forEach(card -> {
                    Card c = new Card(card.getAsString().split(" ")[1],
                            card.getAsString().split(" ")[0]);
                    hand.add(c);
                });
                createCardButtons(hand);
                if (players.indexOf(name) != spectator) {
                    addOtherButtons(hand);
                }
                topLabel_1.setText("");
                topLabel_2.setText("");
                panel.revalidate();
                panel.repaint();
                break;
            }
            case CurrentStich.CURRENT: {
                handleCurrentStich(message);
                break;
            }
            case CurrentStich.LAST: {
            }
            case CurrentStich.SPECIFIC: {
                showLastStich(message);
                break;
            }
            case Wait4Player.COMMAND: {
                if (message.getParams().get("player").getAsString().equals(name)) {
                    topLabel_2.setText("Du bist am Zug");
                    wait4Player = true;
                } else {
                    topLabel_2.setText(message.getParams().get("player").getAsString() + " ist am Zug");
                }
                break;
            }
            case MessageAllPlayers.TYPE: {
                MessageAllPlayers m = MessageAllPlayers.fromString(serverReply);
                playerList.removeAll();
                players = new ArrayList<>();
                DefaultListModel<String> model = new DefaultListModel<>();
                m.getPlayers().forEach(player -> {
                    model.addElement(player);
                    players.add(player);
                });
                playerList.setModel(model);
                if (players.size() > 0) {
                    start.setEnabled(true);
                }
                break;
            }
            case StartGame.COMMAND: {
                m.createUI();
                break;
            }
            case GameEnd.COMMAND: {
                cardPos1.setText("");
                cardPos2.setText("");
                cardPos3.setText("");
                cardPos4.setText("");
                updateTable();
                EndDialog e = new EndDialog(
                        message.getParams().get("re1").getAsString(),
                        message.getParams().get("re2").getAsString(),
                        message.getParams().get("kontra1").getAsString(),
                        message.getParams().get("kontra2").getAsString());
                e.showDialog(this);
                clearTable();
                schweinExists = false;
                selectCards = false;
                SendByTCP(new ReadyForNextRound(players.indexOf(name)));
                break;
            }
            case SelectGame.COMMAND: {
                controlPanel.setVisible(players.indexOf(name) != spectator);
                break;
            }
            case GameType.COMMAND: {
                selectedGame = message.getParams().get(GameType.COMMAND).getAsString();
                if (hand != null && hand.size() > 0) {
                    createCardButtons(SortHand.sort(hand, selectedGame, schweinExists));
                    if (selectedGame.equals(GameSelected.NORMAL)
                            || selectedGame.equals(GameSelected.KARO)
                            || selectedGame.equals(GameSelected.ARMUT)) {
                        if (hand.stream().filter(p -> p.farbe.equals(Statics.KARO) && p.value.equals(Statics.ASS)).count() > 1) {
                            SendByTCP(new SchweinExists());
                            schweinExists = true;
                        } else {
                            schweinExists = false;
                        }
                    }
                }
                break;
            }
            case SelectCards4Armut.COMMAND: {
                selectCards4Armut(SendCards.RICH);
                break;
            }
            case SendCards.COMMAND: {
                List<Card> list = new ArrayList<>();
                message.getParams().get("cards").getAsJsonArray().forEach(card -> list.add(
                        new Card(card.getAsString().split(" ")[1],
                                card.getAsString().split(" ")[0])));
                hand.addAll(list);
                createCardButtons(SortHand.sortNormal(hand, schweinExists));
                if (message.getParams().get("receiver").getAsString().equals(SendCards.RICH)) {
                    selectCards4Armut(SendCards.POOR);
                }
                break;
            }
            case GetArmut.COMMAND: {
                if (JOptionPane.showConfirmDialog(mainFrame, "Armut mitnehmen?",
                        "Armut mitnehmen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    SendByTCP(new GetArmut(players.indexOf(name), true));
                } else {
                    SendByTCP(new GetArmut(players.indexOf(name), false));
                }
                break;
            }
            case DisplayMessage.COMMAND: {
                topLabel_1.setText(message.getParams().get("message").getAsString());
                break;
            }
            case UpdateUserPanel.COMMAND: {
                updateUserPanel(message);
                break;
            }
            case AnnounceSpectator.COMMAND: {
                spectator = message.getParams().get("player").getAsInt();
                break;
            }
            case GetVersion.COMMAND: {
                if (!DokoServer.VERSION.equals(message.getParams().get("version").getAsString())) {
                    JOptionPane.showMessageDialog(createJoinFrame,
                            "Version(Server): " + message.getParams().get("version").getAsString() + "\n" +
                                    "Version(lokal): " + DokoServer.VERSION);
                }
                break;
            }
        }
    }

    private void showLastStich(RequestObject stich) {

        JLabel cardPos1 = new JLabel();
        JLabel cardPos2 = new JLabel();
        JLabel cardPos3 = new JLabel();
        JLabel cardPos4 = new JLabel();
        int ownNumber = players.indexOf(name);
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
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 1) {
                    cardPos1 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 2) {
                    cardPos2 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 3) {
                    cardPos3 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
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

    private void updateUserPanel(RequestObject object) {

        int ownNumber = players.indexOf(name);
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
                userLabel_1.setText(object.getParams().get("player").getAsString()+"\n" +
                        object.getParams().get("text").getAsString());
                break;
            }
            case 2:{
                userLabel_2.setText(object.getParams().get("player").getAsString()+"\n" +
                        object.getParams().get("text").getAsString());
                break;
            }
            case 3:{
                userLabel_3.setText(object.getParams().get("player").getAsString()+"\n" +
                        object.getParams().get("text").getAsString()+"     ");
                break;
            }
            case 0:{
                userLabel_4.setText(object.getParams().get("text").getAsString());
                break;
            }
        }
    }

    private void selectCards4Armut(String receiver) {
        controlPanel.removeAll();
        selectCards=true;
        cards2Send = new ArrayList<>();
        cardLabels2Send = new ArrayList<>();
        String buttonText="";
        if (receiver.equals(SendCards.RICH)){
            buttonText = "Armut anbieten";
            autoSelectArmutCards();
        }
        else if(receiver.equals(SendCards.POOR)){
            buttonText = "Karten zurueckgeben";
        }
        JButton button = new JButton(buttonText);
        button.addActionListener(e -> {
            SendByTCP(new SendCards(cards2Send,receiver));
            hand.removeAll(cards2Send);
            createCardButtons(SortHand.sort(hand,selectedGame,schweinExists));
            cardLabels2Send = new ArrayList<>();
            selectCards = false;
            controlPanel.setVisible(false);
        });
        controlPanel.add(button);
        controlPanel.setVisible(true);
    }

    private void autoSelectArmutCards() {
        labelMap.keySet().forEach(card -> {
            if(card.trumpf) {
                cards2Send.add(card);
                cardLabels2Send.add(labelMap.get(card));
                labelMap.get(card).setBorder(new LineBorder(Color.RED, 3));
            }
        });
    }

    private void handleCurrentStich(RequestObject stich) {
        cardPos1 =new JLabel();
        cardPos2 =new JLabel();
        cardPos3 =new JLabel();
        cardPos4 =new JLabel();
        int ownNumber = players.indexOf(name);
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


        int numbCardsOnTable = 0;
        for(int j = 0;j<tmpList.size();j++) {
            if(stich.getParams().has(String.valueOf(tmpList.get(j)))) {
                numbCardsOnTable++;
                if (j == 0) {
                    cardPos4 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 1) {
                    cardPos1 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 2) {
                    cardPos2 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                } else if (j == 3) {
                    cardPos3 = getCardLabel(new Card(
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[1],
                                    stich.getParams().get(String.valueOf(tmpList.get(j))).getAsString().split(" ")[0]),
                            false);
                }
            }
        }
        if(numbCardsOnTable==1){
            if(letzterStich!=null) {
                letzterStich.dispose();
            }
        }
        updateTable();
    }

    private void createCardButtons(List<Card> cards){
        panel.removeAll();
        labelMap = new HashMap<>();
        cards.forEach(this::createCardButton);
        if(players.indexOf(name)!=spectator){
            addOtherButtons(cards);
        }
        panel.revalidate();
        panel.repaint();
    }

    private JLabel getCardLabel(Card card,boolean bCropped){
        String path = System.getProperty("user.dir")+"\\resources\\" + card.farbe + card.value + ".PNG";
        BufferedImage image;
        ImageIcon icon = null;
        int size = 10;
        if (hand!=null && hand.size()>10){
            size = hand.size();
        }
        try {
            icon = new ImageIcon(ImageIO.read(new File(path)));
            image = ImageIO.read(new File(path));
            int imageWidth = image.getWidth();
            double faktor = ((mainFrame.getSize().getWidth()/size)-6)/(double)imageWidth;
            BufferedImage after = new BufferedImage((int)mainFrame.getSize().getWidth()/size,
                    (int)(mainFrame.getSize().getHeight()-(panel.getSize().getHeight()))/3, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            //at.scale(0.1, 0.1);
            at.scale(faktor, faktor);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(image, after);
            if(bCropped) {
                BufferedImage cropped;
                cropped = after.getSubimage(0, 0, ((int)mainFrame.getSize().getWidth()/size-6), 110);
                icon.setImage(cropped);
            }
            else{
                icon.setImage(after);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        JLabel label = new JLabel(icon);
        label.setSize(new Dimension((int)mainFrame.getSize().getWidth()/size,110));
        return label;
    }

    private void createCardButton(Card card) {
        JLabel label = getCardLabel(card,true);
        labelMap.put(card,label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(selectCards){
                    if(cardLabels2Send.size()<3 && !cardLabels2Send.contains(label)){
                        cards2Send.add(card);
                        cardLabels2Send.add(label);
                        label.setBorder(new LineBorder(Color.RED,3));
                    }
                    else {
                        if(cardLabels2Send.contains(label)){
                            cards2Send.remove(card);
                            cardLabels2Send.remove(label);
                            label.setBorder(new LineBorder(Color.BLACK,3));
                        }
                    }
                }
                else {
                    if (wait4Player) {
                        wait4Player = false;
                        hand.remove(card);
                        label.setVisible(false);
                        cardPos4 = getCardLabel(card,false);
                        updateTable();
                        m.SendByTCP(new PutCard(card.farbe, card.value));
                        topLabel_1.setText("");
                        if (hand!=null) {
                            createCardButtons(SortHand.sort(hand,selectedGame,schweinExists));
                        }
                    }
                }
            }
        });
        panel.add(label);
        Dimension d = new Dimension(mainFrame.getWidth(),label.getHeight());
        panel.setPreferredSize(d);
        panel.setMaximumSize(d);
        panel.setMinimumSize(d);
        panel.setSize(d);
    }

    private void addOtherButtons(List<Card> cards){

        controlPanel.removeAll();
        sortNormal = new JButton("Gesund");
        sortBuben = new JButton("Buben");
        sortDamen = new JButton("Damen");
        sortBubenDamen = new JButton("Buben-Damen");
        sortFleisch = new JButton("Fleischlos");
        sortKreuz = new JButton("Kreuz");
        sortPik = new JButton("Pik");
        sortHerz = new JButton("Herz");
        sortKaro = new JButton("Karo");
        sortArmut = new JButton("Armut");
        hochzeit = new JButton("Hochzeit");
        koenige = new JButton(">4 Koenige");
        JButton vorbehalt = new JButton("OK");
        vorbehalt.addActionListener(e ->{
            SendByTCP(new GameSelected(players.indexOf(name),selectedGame));
            controlPanel.setVisible(false);
            controlPanel.removeAll();
        });
        //controlPanel.setVisible(true);

        sortNormal.addActionListener(e->{
            createCardButtons(SortHand.sortNormal(hand, schweinExists));
            deselectAllSortButtons();
            sortNormal.setBackground(Color.GREEN);
            selectedGame = GameSelected.NORMAL;
        });
        sortDamen.addActionListener(e->{
            createCardButtons(SortHand.sortDamenSolo(hand));
            deselectAllSortButtons();
            sortDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.DAMEN;
        });
        sortBuben.addActionListener(e->{
            createCardButtons(SortHand.sortBubenSolo(hand));
            deselectAllSortButtons();
            sortBuben.setBackground(Color.GREEN);
            selectedGame = GameSelected.BUBEN;
        });
        sortBubenDamen.addActionListener(e->{
            createCardButtons(SortHand.sortBubenDamenSolo(hand));
            deselectAllSortButtons();
            sortBubenDamen.setBackground(Color.GREEN);
            selectedGame = GameSelected.BUBENDAMEN;
        });
        sortFleisch.addActionListener(e->{
            createCardButtons(SortHand.sortFleischlos(hand));
            deselectAllSortButtons();
            sortFleisch.setBackground(Color.GREEN);
            selectedGame = GameSelected.FLEISCHLOS;
        });
        sortKreuz.addActionListener(e -> {
            createCardButtons(SortHand.sortKreuz(hand));
            deselectAllSortButtons();
            sortKreuz.setBackground(Color.GREEN);
            selectedGame = GameSelected.KREUZ;
        });
        sortPik.addActionListener(e -> {
            createCardButtons(SortHand.sortPik(hand));
            deselectAllSortButtons();
            sortPik.setBackground(Color.GREEN);
            selectedGame = GameSelected.PIK;
        });
        sortHerz.addActionListener(e -> {
            createCardButtons(SortHand.sortHerz(hand));
            deselectAllSortButtons();
            sortHerz.setBackground(Color.GREEN);
            selectedGame = GameSelected.HERZ;
        });
        sortKaro.addActionListener(e -> {
            createCardButtons(SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            sortKaro.setBackground(Color.GREEN);
            selectedGame = GameSelected.KARO;
        });
        sortArmut.addActionListener(e -> {
            createCardButtons(SortHand.sortArmut(hand, schweinExists));
            deselectAllSortButtons();
            sortArmut.setBackground(Color.GREEN);
            selectedGame = GameSelected.ARMUT;
        });
        koenige.addActionListener(e->{
            createCardButtons(SortHand.sortKaro(hand, schweinExists));
            deselectAllSortButtons();
            koenige.setBackground(Color.GREEN);
            selectedGame = GameSelected.KOENIGE;
        });
        hochzeit.addActionListener(e -> {
            createCardButtons(SortHand.sortNormal(hand,schweinExists));
            deselectAllSortButtons();
            hochzeit.setBackground(Color.GREEN);
            selectedGame = GameSelected.HOCHZEIT;
        });


        controlPanel.add(sortNormal);
        controlPanel.add(sortDamen);
        controlPanel.add(sortBuben);
        controlPanel.add(sortBubenDamen);
        controlPanel.add(sortFleisch);
        controlPanel.add(sortKreuz);
        controlPanel.add(sortPik);
        controlPanel.add(sortHerz);
        controlPanel.add(sortKaro);
        if(cards.stream().filter(p->p.trumpf).count()<4){
            controlPanel.add(sortArmut);
        }
        if(cards.stream().filter(p->p.value.equals(Statics.KOENIG)).count()>4) {
            controlPanel.add(koenige);
        }
        if(cards.stream().filter(p->p.value.equals(Statics.DAME)&&p.farbe.equals(Statics.KREUZ)).count()>1){
            controlPanel.add(hochzeit);
        }
        controlPanel.add(vorbehalt);
        Dimension d = new Dimension(mainFrame.getSize().width,mainFrame.getSize().height/(16));
        controlPanel.setMaximumSize(d);
        controlPanel.setMinimumSize(d);
        controlPanel.setPreferredSize(d);
        controlPanel.setSize(d);
    }

    private void deselectAllSortButtons(){
        sortNormal.setBackground(Color.BLACK);
        sortDamen.setBackground(Color.BLACK);
        sortBuben.setBackground(Color.BLACK);
        sortBubenDamen.setBackground(Color.BLACK);
        sortFleisch.setBackground(Color.BLACK);
        sortKreuz.setBackground(Color.BLACK);
        sortPik.setBackground(Color.BLACK);
        sortHerz.setBackground(Color.BLACK);
        sortKaro.setBackground(Color.BLACK);
        sortArmut.setBackground(Color.BLACK);
        koenige.setBackground(Color.BLACK);
    }


    public void sendingThread(){
        new Thread(() -> {
            while (true) {
                try {
                    ev.waitOne();
                    if(lastMessage!=null) {
                        log.info("resending last message");
                        SendByTCP(lastMessage, false);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void SendByTCP(RequestObject message) {
        SendByTCP(message,true);
    }

    public void SendByTCP(RequestObject message, boolean retry){
        log.info("Sending command: "+ message.getCommand());
        new Thread(() -> {
            lastMessage = null;
            while (socket==null){
                log.info("Connection was lost. Try to Reconnect");
                openTCPConnection(getHostname(),getPort(),false);
            }
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(message.toJson());
            } catch (IOException e) {
                log.info("Error while sending Message: " + e.getMessage());
                if (retry) {
                    lastMessage = message;
                    ev.set();
                }
            }
        }).start();
    }


    private void createUI(){

        mainFrame = new JFrame("Doppelkopf Version "+DokoServer.VERSION + " " + name );
        mainPanel =new JPanel(new GridBagLayout());
        topPanel = new JPanel(new GridLayout(1,3));
        GridBagConstraints c = new GridBagConstraints();
        topLabel_1 = new JLabel("");
        topLabel_2 = new JLabel("");

        table = new JPanel(new GridLayout(3,5));
        panel = new JPanel(new GridLayout(1,14));
        controlPanel = new JPanel(new GridLayout(1,6));



        c.gridx=0;
        c.gridy=0;
        c.gridheight=5;
        c.gridwidth=1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(table,c);
        c.gridx=0;
        c.gridy=5;
        c.gridheight=1;
        c.gridwidth=1;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(topPanel,c);
        c.gridx=0;
        c.gridy=6;
        c.gridheight=4;
        c.gridwidth=1;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(panel,c);
        c.gridx=0;
        c.gridy=10;
        c.gridheight=1;
        c.gridwidth=1;
        c.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(controlPanel,c);
        cardPos1 = new JLabel();
        cardPos2 = new JLabel();
        cardPos3 = new JLabel();
        cardPos4 = new JLabel();
        updateTable();
        controlPanel.setVisible(false);

        mainFrame.add(mainPanel);
        createJoinFrame.setVisible(false);
        mainFrame.pack();
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int reply = JOptionPane.showOptionDialog(mainFrame,"Wirklich beenden?", "Beenden?",
                        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,new String[]{"Ja","Nein"},
                        null);
                if (reply == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (e.getComponent().getSize().getWidth()!=frameSize.getWidth()){
                    frameSize = e.getComponent().getSize();
                    if (hand!=null) {
                        createCardButtons(SortHand.sortNormal(hand, schweinExists));
                    }
                }
            }
        });

    }

    private void clearTable(){
        cardPos1 = new JLabel();
        cardPos2 = new JLabel();
        cardPos3 = new JLabel();
        cardPos4 = new JLabel();
        updateTable();
    }

    private void updateTable() {
        table.removeAll();
        //1x1
        table.add(new JLabel());
        //2x1
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(userLabel_2,BorderLayout.EAST);
        table.add(panel2);
        //3x1
        table.add(cardPos2);
        //4x1
        table.add(new JLabel());
        //5x1
        table.add(new JLabel());
        //1x2
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(userLabel_1,BorderLayout.WEST);
        table.add(panel1);
        //2x2
        table.add(cardPos1);
        //3x2
        table.add(new JLabel());
        //4x2
        table.add(cardPos3);
        //5x2
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(userLabel_3,BorderLayout.EAST);
        table.add(panel3);
        //1x3
        JPanel tableButtons = new JPanel(new GridLayout(3,1));
        JButton button_lastStich = new JButton("letzter Stich");
        button_lastStich.addActionListener(e -> SendByTCP(new CurrentStich(new HashMap<>(),players.indexOf(name),true)));
        JButton button_clearTable = new JButton("Tisch leeren");
        button_clearTable.addActionListener(e -> clearTable());
        tableButtons.add(button_lastStich);
        tableButtons.add(button_clearTable);
        if (isAdmin){
            JButton button_adminPanel = new JButton("Admin");
            tableButtons.add(button_adminPanel);
            button_adminPanel.addActionListener(e -> {
                admin_panel.dispose();
                createAdminUI();
            });
        }
        table.add(tableButtons);
        //2x3
        table.add(new JLabel());
        //3x3
        table.add(cardPos4);
        //4x3
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(userLabel_4,BorderLayout.SOUTH);
        table.add(panel4);
        //5x3
        JPanel panel5 = new JPanel(new GridLayout(2,1));
        panel5.add(topLabel_1);
        panel5.add(topLabel_2);
        table.add(panel5);
        //
        table.revalidate();
        table.repaint();
    }
}
