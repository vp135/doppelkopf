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
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private final Logger log = new Logger(this.getClass().getName(),1);

    Socket socket;
    static Main m;
    JPanel panel;
    JPanel mainPanel;
    JPanel table;
    JLabel serverMessageLabel;
    JLabel gameMessageLabel;
    JFrame mainFrame;

    private final Random random = new Random(System.currentTimeMillis());

    private String selectedGame = GameSelected.NORMAL;
    private List<Card> hand;
    private String name;
    private JPanel controlPanel;
    private List<String> players = new ArrayList<>();
    private final JList<String> playerList = new JList<>();
    private JLabel userLabel_1;
    private JLabel userLabel_2;
    private JLabel userLabel_3;
    private JLabel userLabel_4;
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

    private Map<Card,JLabel> labelMap;

    private final AutoResetEvent ev = new AutoResetEvent(true);
    private boolean isAdmin;
    private JFrame admin_panel;
    private int cardSize;
    private JLabel tableLable;
    private Graphics playArea;
    private JLayeredPane layeredPane;
    private Configuration c;
    private final ConcurrentLinkedDeque<RequestObject> outMessages = new ConcurrentLinkedDeque<>();
    private ServerConfig serverConfig = new ServerConfig();
    private JButton sendCardsButton;
    private int armutCardCount;
    private final AtomicBoolean wait = new AtomicBoolean(true);



    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }


    public static void main(String[] args) {
        modifyUIManager();
        m = new Main();
        m.createOrJoin();
    }

    private static void modifyUIManager() {
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
    }

    public Main(){
        outMessageHandling();
    }

    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    ev.waitOne(DokoServer.TIMEOUT);
                    if((socket==null || socket.isClosed())&&!wait.get()){
                        openTCPConnection(hostname,port);
                    }
                    if(socket!=null && socket.isConnected()) {
                        while (outMessages.peek() != null) {
                            if (SendByTCP(outMessages.peek())) {
                                outMessages.poll();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }
            }
        }).start();
    }

    private boolean SendByTCP(RequestObject requestObject) {
        boolean sent = false;
        if (socket!=null && !socket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                String s = requestObject.toJson();
                log.info("Sending to server: " + requestObject.getCommand());
                out.println(s);
                sent = true;
            } catch (IOException ex) {
                log.error(ex.toString());
                //ev.set();
            }
        }
        else{
            log.warn("socket was unexpectedly closed - Trying to reopen connection");
            socket = null;
            //openTCPConnection(hostname, port);
        }
        return sent;
    }


    public void createOrJoin() {
        c = Configuration.fromFile();
        log.setLoglevel(c.logLevel);
        createJoinFrame = new JFrame();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JPanel inputs = new JPanel(new GridLayout(5, 2));
        JPanel rightPanel = new JPanel(new GridLayout(3,1));

        JPanel userOptions = new JPanel(new GridLayout(6,2));
        userOptions.add(new JLabel("Kartenwinkel (rechts/links)"));
        JTextField angle24Field = new JTextField();
        userOptions.add(angle24Field);
        userOptions.add(new JLabel("Kartenwinkel (oben/unten)"));
        JTextField angle13Field = new JTextField();
        userOptions.add(angle13Field);
        userOptions.add(new JLabel("max Abweichung des Winkels"));
        JTextField angleVariationField = new JTextField();
        userOptions.add(angleVariationField);
        userOptions.add(new JLabel("Relativer Abstand der Karten zur Tischmitte"));
        JTextField distanceField = new JTextField();
        userOptions.add(distanceField);
        userOptions.add(new JLabel("max relative Abweichung des Abstands"));
        JTextField distanceVariationField = new JTextField();
        userOptions.add(distanceVariationField);
        JButton optionsTestButton = new JButton("Einstellungen Testen");
        userOptions.add( optionsTestButton);


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
        rightPanel.add(playerList);
        rightPanel.add(userOptions);
        panel.add(rightPanel);
        inputs.add(create);
        inputs.add(join);

        optionsTestButton.addActionListener(e -> {
            overrideConfig(angle24Field, angle13Field, angleVariationField, distanceField,
                    distanceVariationField, hostname, port, false);
            createOptionsTestFrame();
        });

        if (c != null) {
            playername.setText(c.name);
            hostname.setText(c.server);
            port.setText(String.valueOf(c.port));
            angle24Field.setText(String.valueOf(c.angle24));
            angle13Field.setText(String.valueOf(c.angle13));
            angleVariationField.setText(String.valueOf(c.angleVariation));
            distanceField.setText(String.valueOf(c.distanceFromCenter));
            distanceVariationField.setText(String.valueOf(c.distanceVariation));

        }

        create.addActionListener(e -> {
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
                name = playername.getText();
                dokoServer = new DokoServer(Integer.parseInt(port.getText()),c);
                create.setEnabled(false);
                join.setEnabled(false);
                while(!dokoServer.listening) {
                    log.info("not listening");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        log.error(interruptedException.toString());
                    }
                }
                openTCPConnection("127.0.0.1", Integer.parseInt(port.getText()));
                queueOutMessage(new GetVersion(name,DokoServer.VERSION));
                inputs.add(start);
            } else if (!port.getText().trim().equals("")) {
                name = playername.getText();
                dokoServer = new DokoServer(Integer.parseInt(port.getText()),c);
                inputs.add(start);
            }
            overrideConfig(angle24Field, angle13Field,
                    angleVariationField, distanceField,
                    distanceVariationField, hostname,
                    port, true);
        });

        join.addActionListener(e -> {
            this.hostname = hostname.getText();
            this.port = Integer.parseInt(port.getText());
            new Thread(() -> {
                name = playername.getText().trim();
                if (!name.equals("")) {
                    openTCPConnection(hostname.getText(), Integer.parseInt(port.getText()));
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
                            log.error(interruptedException.toString());
                        }
                    }
                    if (socket != null) {
                        join.setText("verbunden");
                        log.info("verbunden");
                        overrideConfig(angle24Field, angle13Field,
                                angleVariationField, distanceField,
                                distanceVariationField, hostname,
                                port, true);
                        create.setEnabled(false);
                        queueOutMessage(new GetVersion(name,DokoServer.VERSION));
                    } else {
                        join.setText("beitreten");
                        log.info("beitreten");
                    }
                }
            }).start();

        });
        start.addActionListener(e -> {
            isAdmin = true;
            dokoServer.startGame();
        });
        createJoinFrame.pack();
        createJoinFrame.add(panel);
        createJoinFrame.setVisible(true);
        createJoinFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        createJoinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void createOptionsTestFrame() {
        JPanel panel = new JPanel();
        setComponentSizes(panel, new Dimension(createJoinFrame.getWidth(), createJoinFrame.getHeight() / 15 * 10));
        panel.setBackground(new Color(0,0,0,255));
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.drawImage(img, 0, 0, panel.getHeight(), panel.getWidth(), null);
        int size = createJoinFrame.getHeight()/30*8;
        JLabel label = new JLabel(new ImageIcon(img));
        label.setBackground(new Color(0,0,0,255));
        panel.add(label);
        for(int i = 0;i<4;i++){
            drawCard2Position(Card.randomCard(),size,i,g,panel.getHeight(),panel.getWidth());
        }
        JFrame testFrame = new JFrame("Test");
        testFrame.add(panel);
        testFrame.pack();
        testFrame.setVisible(true);
    }

    private void overrideConfig(JTextField angle24Field, JTextField angle13Field,
                                JTextField angleVariationField, JTextField distanceField,
                                JTextField distanceVariationField, JTextField hostname,
                                JTextField port, boolean save) {
        c.name = name;
        c.server = hostname.getText();
        c.port = Integer.parseInt(port.getText());
        c.angle24 = Integer.parseInt(angle24Field.getText());
        c.angle13 = Integer.parseInt(angle13Field.getText());
        c.angleVariation = Integer.parseInt(angleVariationField.getText());
        c.distanceFromCenter = Integer.parseInt(distanceField.getText());
        c.distanceVariation = Integer.parseInt(distanceVariationField.getText());
        if(save) {
            c.saveConfig();
        }
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

        button_showStich.addActionListener(e -> {
            Stich stich = dokoServer.getStich(Integer.parseInt(stichNumber.getText()));
            stich.check4ExtraPoints();
            log.info(stich.getExtraPoints());
        });

        button_abortGame.addActionListener(e -> queueOutMessage(new AbortGame()));

        JButton button_selectGame = new JButton("Spiel auswählen");
        button_selectGame.addActionListener(e -> dokoServer.send2All(new SelectGame()));

        adminMainPanel.add(button_selectGame);

        JButton button_close = new JButton("close");
        button_close.addActionListener(e -> admin_panel.dispose());
    }
    public void openTCPConnection(String hostname, int port) {
        queueOutMessage(new AddPlayer(name));
        wait.set(true);
        new Thread(() -> {
                while (socket == null) {
                    try {
                        socket = new Socket(hostname, port);
                        Listen();
                        log.info("Connected to Server");
                    } catch (IOException e) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        log.warn("Could not connect to Server: " + e);
                    }
            }
            wait.set(false);
        }).start();

    }

    private void Listen() {
        new Thread(() -> {
            if (socket != null) {
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
                                    handleInput(ServerReply);
                                }
                            }
                        } catch (Exception ex) {
                            log.error(ex.toString());
                            socket = null;
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
                handlePlayersInLobby(message);
                break;
            }
            case Cards.COMMAND: {
                handleCards(message);
                break;
            }
            case PutCard.COMMAND: {
                handlePutCard(message);
                break;
            }
            case CurrentStich.LAST: { }
            case CurrentStich.SPECIFIC: {
                showLastStich(message);
                break;
            }
            case Wait4Player.COMMAND: {
                handleWait4Player(message);
                break;
            }
            case MessageAllPlayers.TYPE: {
                handleMessageAllPlayers(serverReply);
                break;
            }
            case StartGame.COMMAND: {
                m.createUI();
                break;
            }
            case GameEnd.COMMAND: {
                handleGameEnd(message);
                break;
            }
            case SelectGame.COMMAND: {
                controlPanel.setVisible(players.indexOf(name) != spectator);
                //createCardButtons(hand);
                break;
            }
            case GameType.COMMAND: {
                handleGameType(message);
                break;
            }
            case SelectCards4Armut.COMMAND: {
                selectCards4Armut(SendCards.RICH);
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
            case DisplayMessage.COMMAND: {
                serverMessageLabel.setText(message.getParams().get("message").getAsString());
                break;
            }
            case UpdateUserPanel.COMMAND: {
                updateUserPanel(message);
                break;
            }
            case AnnounceSpectator.COMMAND: {
                handleAnnounceSpectator(message);
                break;
            }
            case GetVersion.COMMAND: {
                handleGetVersion(message);
                break;
            }
        }
    }

    private void handleAnnounceSpectator(RequestObject message) {
        spectator = message.getParams().get("player").getAsInt();
        if(players.size()>5 && players.get(spectator).equals(name)) {
            queueOutMessage(new DisplayMessage("Du bist jetzt Zuschauer"));
            hand.clear();
            clearPlayArea();
            createCardButtons(hand);
        }
    }

    private void handleGetVersion(RequestObject message) {
        if (!DokoServer.VERSION.equals(message.getParams().get("version").getAsString())) {
            JOptionPane.showMessageDialog(createJoinFrame,
                    "Version(Server): " + message.getParams().get("version").getAsString() + "\n" +
                            "Version(lokal): " + DokoServer.VERSION);
        }
    }

    private void handleWait4Player(RequestObject message) {
        if (message.getParams().get("player").getAsString().equals(name)) {
            gameMessageLabel.setText("Du bist am Zug");
            wait4Player = true;
        } else {
            gameMessageLabel.setText(message.getParams().get("player").getAsString() + " ist am Zug");
        }
    }

    private void handleGetArmut() {
        if (JOptionPane.showConfirmDialog(mainFrame, "Armut mitnehmen?",
                "Armut mitnehmen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            queueOutMessage(new GetArmut(players.indexOf(name), true));
        } else {
            queueOutMessage(new GetArmut(players.indexOf(name), false));
        }
    }

    private void handleSendCards(RequestObject message) {
        List<Card> list = new ArrayList<>();
        message.getParams().get("cards").getAsJsonArray().forEach(card -> list.add(
                new Card(card.getAsString().split(" ")[1],
                        card.getAsString().split(" ")[0])));
        hand.addAll(list);
        createCardButtons(SortHand.sortNormal(hand, schweinExists));
        if (message.getParams().get("receiver").getAsString().equals(SendCards.RICH)) {
            selectCards4Armut(SendCards.POOR, list.size());
        }
    }

    private void handleGameType(RequestObject message) {
        selectedGame = message.getParams().get(GameType.COMMAND).getAsString();
        if (hand != null && hand.size() > 0) {
            createCardButtons(SortHand.sort(hand, selectedGame, schweinExists));
            if (selectedGame.equals(GameSelected.NORMAL)
                    || selectedGame.equals(GameSelected.KARO)
                    || selectedGame.equals(GameSelected.ARMUT)) {
                if (hand.stream().filter(p -> p.farbe.equals(Statics.KARO)
                        && p.value.equals(Statics.ASS)).count() > 1) {
                    queueOutMessage(new SchweinExists());
                    schweinExists = true;
                } else {
                    schweinExists = false;
                }
            }
        }
    }

    private void handleGameEnd(RequestObject message) {
        updateTable();
        EndDialog e = new EndDialog(
                message.getParams().get("re1").getAsString(),
                message.getParams().get("re2").getAsString(),
                message.getParams().get("kontra1").getAsString(),
                message.getParams().get("kontra2").getAsString());
        e.showDialog(this);
        clearPlayArea();
        schweinExists = false;
        selectCards = false;
        queueOutMessage(new ReadyForNextRound(players.indexOf(name)));
    }

    private void handleMessageAllPlayers(String serverReply) {
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
    }

    private void handleCards(RequestObject message) {
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
        addOtherButtons(hand);
        serverMessageLabel.setText("");
        gameMessageLabel.setText("");
        panel.revalidate();
        panel.repaint();
    }

    private void handlePlayersInLobby(RequestObject message) {
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
    }

    private void showLastStich( RequestObject stich) {

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

    private JLabel getCardLabel(Card card){
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
            icon.setImage(after);
        } catch (Exception e) {
            log.error(e.toString());
        }
        JLabel label = new JLabel(icon);
        label.setSize(new Dimension((int)mainFrame.getSize().getWidth()/size,110));
        return label;
    }


    private void drawCard2Position(Card card,int cardSize, int pos, Graphics graphics, int canvasSize){
        drawCard2Position(card,cardSize,pos,graphics,canvasSize,canvasSize);
    }

    private void drawCard2Position(Card card,int cardSize, int pos, Graphics graphics, int canvasHeight, int canvasWidth){
        int cardHeight = cardSize;
        AffineTransform at;
        int distFromCenter = cardSize*c.distanceFromCenter/100;
        int theta = c.angleVariation - random.nextInt(c.angleVariation*2 + 1);
        int distVar = distFromCenter +  c.distanceVariation - random.nextInt(c.distanceVariation*2 + 1);
        BufferedImage img =getIcon4Table(card,cardHeight);
        int halfHeight = canvasHeight/2;
        int halfWidth = canvasWidth/2;
        int anchorY = halfHeight;
        int anchorX = halfWidth;
        switch (pos){
            case 0:
                anchorY= halfHeight + distVar;
                theta += c.angle13;
                break;
            case 1:
                anchorX = halfWidth - distVar;
                theta += c.angle24;
                break;
            case 2:
                anchorY = halfHeight - distVar;
                theta += c.angle13;
                break;
            case 3:
                anchorX = halfWidth + distVar;
                theta += c.angle24;
                break;
        }
        at = AffineTransform.getRotateInstance(Math.toRadians(theta),anchorX,anchorY);
        at.translate(anchorX-(cardHeight*0.67/2),anchorY-cardHeight/2);
        Graphics2D g = (Graphics2D) graphics;
        g.drawImage(img, at, null);
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
                userLabel_1.setText(object.getParams().get("text").getAsString());
                break;
            }
            case 2:{
                userLabel_2.setText(object.getParams().get("text").getAsString());
                break;
            }
            case 3:{
                userLabel_3.setText(object.getParams().get("text").getAsString());
                break;
            }
            case 0:{
                userLabel_4.setText(object.getParams().get("text").getAsString());
                break;
            }
        }
        updateTable();
    }

    private void selectCards4Armut(String receiver){
        selectCards4Armut(receiver,-1);
    }

    private void selectCards4Armut(String receiver, int count) {
        armutCardCount = count;
        controlPanel.removeAll();
        selectCards = true;
        cards2Send = new ArrayList<>();
        cardLabels2Send = new ArrayList<>();
        String buttonText="";
        if (receiver.equals(SendCards.RICH)){
            buttonText = "Armut anbieten";
            autoSelectArmutCards();
        }
        else if(receiver.equals(SendCards.POOR)){
            buttonText = count + " Karten zurueckgeben";
        }
        sendCardsButton = new JButton(buttonText);
        if(count>-1 && serverConfig.checkNumberOfArmutCards){
            sendCardsButton.setEnabled(false);
        }
        sendCardsButton.addActionListener(e -> {
            queueOutMessage(new SendCards(cards2Send,receiver));
            hand.removeAll(cards2Send);
            createCardButtons(SortHand.sort(hand,selectedGame,schweinExists));
            cardLabels2Send = new ArrayList<>();
            selectCards = false;
            controlPanel.setVisible(false);
        });
        controlPanel.add(sendCardsButton);
        controlPanel.setVisible(true);
    }

    private void autoSelectArmutCards() {
        labelMap.keySet().forEach(card -> {
            if(card.trumpf) {
                cards2Send.add(card);
                cardLabels2Send.add(labelMap.get(card));
                labelMap.get(card).setBorder(new LineBorder(Color.RED, 2));
            }
        });
    }

    int currentCardsOnTable = 0;

    private void handlePutCard(RequestObject object){
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

        if(currentCardsOnTable>3){
            clearPlayArea();
            if(letzterStich!=null) {
                letzterStich.dispose();
            }
            updateTable();
            currentCardsOnTable = 0;
        }

        int player = object.getParams().get("player").getAsInt();
        Card card = new Card(
                object.getParams().get("wert").getAsString(),
                object.getParams().get("farbe").getAsString());

        for(int j :tmpList){
            if(player==j){
                drawCard2Position(card,cardSize, tmpList.indexOf(j),playArea, table.getHeight(), table.getWidth());
                break;
            }
        }

        currentCardsOnTable++;

        updateTable();

    }

    private void createCardButtons(List<Card> cards) {
        panel.removeAll();
        labelMap = new HashMap<>();
        setComponentSizes(panel,new Dimension((int)(cardSize*0.67*hand.size()),panel.getHeight()));
        cards.forEach(this::getCardLabel4Hand);
        panel.revalidate();
        panel.repaint();
    }

    private BufferedImage getIcon4Table(Card card, int cardHeight){
        String path = System.getProperty("user.dir")+"\\resources\\" + card.farbe + card.value + ".PNG";
        BufferedImage image = null;
        ImageIcon icon;
        try {
            icon = new ImageIcon(ImageIO.read(new File(path)));
            image = ImageIO.read(new File(path));
            int cardWidth = (int)(cardHeight*0.67);
            double faktor = (double)cardWidth/(double)image.getWidth();
            BufferedImage after = new BufferedImage(
                    cardWidth,
                    cardHeight,
                    BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(faktor, faktor);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(image, after);
            image = after;
            icon.setImage(after);
        } catch (Exception e) {
            log.warn(e.toString());
        }
        return image;
    }

    private void getCardLabel4Hand(Card card){
        String path = System.getProperty("user.dir")+"\\resources\\" + card.farbe + card.value + ".PNG";
        JLabel label = new JLabel();
        BufferedImage image;
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ImageIO.read(new File(path)));
            image = ImageIO.read(new File(path));
            int cardWidth4Hand = panel.getWidth()/hand.size();
            int cardHeight4Hand = (int)((double)cardWidth4Hand/0.67);
            int cardHeight = Math.min(cardHeight4Hand, mainFrame.getHeight() / 30 * 8);

            int cardWidth = (int)(cardHeight*0.67);
            setComponentSizes(label,new Dimension(cardWidth,cardHeight));
            double faktor = (double)cardWidth/(double)image.getWidth();
            BufferedImage after = new BufferedImage(
                    cardWidth,
                    cardHeight,
                    BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(faktor, faktor);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(image, after);
            icon.setImage(after);
        } catch (Exception e) {
            log.warn(e.toString());
        }
        label.setIcon(icon);

        labelMap.put(card,label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectCards) {
                    if (cardLabels2Send.size() < armutCardCount && !cardLabels2Send.contains(label)) {
                        cards2Send.add(card);
                        cardLabels2Send.add(label);
                        label.setBorder(new LineBorder(Color.RED,2));
                    } else {
                        if (cardLabels2Send.contains(label)) {
                            cards2Send.remove(card);
                            cardLabels2Send.remove(label);
                            label.setBorder(new LineBorder(Color.BLACK,2));
                        }
                    }
                    sendCardsButton.setEnabled(cardLabels2Send.size() == armutCardCount);
                } else {
                    if (wait4Player) {
                        wait4Player = false;
                        hand.remove(card);
                        label.setVisible(false);
                        m.queueOutMessage(new PutCard(players.indexOf(name), card.farbe, card.value));
                        serverMessageLabel.setText("");
                        createCardButtons(hand);
                    }
                }
            }
        });
        panel.add(label);
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
            queueOutMessage(new GameSelected(players.indexOf(name),selectedGame));
            controlPanel.setVisible(false);
            controlPanel.removeAll();
        });

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
        Dimension d = new Dimension(mainFrame.getSize().width,mainFrame.getSize().height/(30));
        controlPanel.setMaximumSize(d);
        controlPanel.setMinimumSize(d);
        controlPanel.setPreferredSize(d);
        controlPanel.setSize(d);
        controlPanel.setVisible(true);
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


    public void queueOutMessage(RequestObject message){
        log.info("queue: " + message.getCommand());
        if(message.getCommand().equals(AddPlayer.COMMAND)){
            outMessages.forEach(requestObject -> {
                if(requestObject.getCommand().equals(AddPlayer.COMMAND)){
                    outMessages.remove(requestObject);
                }
            });
            outMessages.addFirst(message);
        }
        else{
            outMessages.offer(message);
        }
        ev.set();
    }


    private void createUI(){
        mainFrame = new JFrame("Doppelkopf Version "+DokoServer.VERSION + " " + name );
        mainPanel =new JPanel(new GridBagLayout());
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.add(mainPanel);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        cardSize = mainFrame.getHeight()/30*8;
        GridBagConstraints c = new GridBagConstraints();
        serverMessageLabel = new JLabel("");
        gameMessageLabel = new JLabel("");

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        layeredPane.setSize(layeredPane.getPreferredSize());
        layeredPane.setMinimumSize(layeredPane.getPreferredSize());
        layeredPane.setMaximumSize(layeredPane.getPreferredSize());
        createPlayArea();
        createHUD();

        panel = new JPanel(new GridLayout(1,14));
        controlPanel = new JPanel(new GridLayout(1,6));

        c.gridx=0;
        c.gridy=0;
        c.gridheight=20;
        c.gridwidth=1;
        c.weighty=20;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        mainPanel.add(layeredPane,c);
        c.gridx=0;
        c.gridy=20;
        c.weighty=8;
        c.gridheight=8;
        c.gridwidth=1;
        //c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(panel,c);
        setComponentSizes(panel,new Dimension(mainFrame.getWidth(),mainFrame.getHeight()/30*8));
        c.gridx=0;
        c.gridy=28;
        c.gridheight=2;
        c.gridwidth=1;
        c.weighty=2;
        c.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(controlPanel,c);
        setComponentSizes(controlPanel,new Dimension(mainFrame.getWidth(),mainFrame.getHeight()/15));

        controlPanel.setVisible(false);

        createJoinFrame.setVisible(false);

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

    private void setComponentSizes(JComponent p, Dimension d){
        p.setPreferredSize(d);
        p.setSize(p.getPreferredSize());
        p.setMinimumSize(p.getPreferredSize());
        p.setMaximumSize(p.getPreferredSize());
    }

    private void createHUD() {
        JPanel hud = new JPanel(new GridLayout(3,3));
        setComponentSizes(hud, new Dimension(layeredPane.getWidth()-15, mainFrame.getHeight() / 15 * 10));
        hud.setBackground(new Color(0,0,0,0));



        userLabel_1= new JLabel();
        userLabel_1.setOpaque(true);
        userLabel_1.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
        userLabel_2 = new JLabel();
        userLabel_2.setOpaque(true);
        userLabel_2.setVerticalAlignment(SwingConstants.TOP);
        userLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel_3 = new JLabel();
        userLabel_3.setOpaque(true);
        userLabel_3.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel_4 = new JLabel();
        userLabel_4.setOpaque(true);
        userLabel_4.setVerticalAlignment(SwingConstants.BOTTOM);
        userLabel_4.setHorizontalAlignment(SwingConstants.CENTER);


        hud.add(new JLabel());
        hud.add(userLabel_2);
        hud.add(new JLabel());
        hud.add(userLabel_1);
        hud.add(new JLabel());
        hud.add(userLabel_3);
        hud.add(createControlButtonPanel());
        hud.add(userLabel_4);
        hud.add(createMessageLabelPanel());


        layeredPane.add(hud,2);
    }

    BufferedImage img;
    private void createPlayArea() {
        table = new JPanel();
        setComponentSizes(table, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        table.setBackground(new Color(0,0,0,0));
        img = new BufferedImage(table.getWidth(), table.getWidth(), BufferedImage.TYPE_INT_ARGB);
        playArea = img.getGraphics();
        playArea.drawImage(img, 0, 0, table.getHeight(), table.getWidth(), null);
        tableLable = new JLabel(new ImageIcon(img));
        table.add(tableLable);
        layeredPane.add(table,1);
    }

    private void clearPlayArea(){
        img = new BufferedImage(table.getWidth(), table.getWidth(), BufferedImage.TYPE_INT_ARGB);
        playArea = img.getGraphics();
        playArea.drawImage(img, 0, 0, table.getHeight(), table.getWidth(), null);
        tableLable = new JLabel(new ImageIcon(img));
        table.removeAll();
        table.add(tableLable);
        updateTable();
    }


    private JPanel createControlButtonPanel(){
        JPanel tableButtons = new JPanel(new GridLayout(1, 3));
        JPanel buttons = new JPanel(new GridLayout(3, 1));
        JButton button_lastStich = new JButton("letzter Stich");
        button_lastStich.addActionListener(e -> queueOutMessage(new CurrentStich(new HashMap<>(), players.indexOf(name), true)));
        //button_lastStich.addActionListener(e -> showLastStich());
        JButton button_clearTable = new JButton("Tisch leeren");
        button_clearTable.addActionListener(e -> clearPlayArea());
        buttons.add(button_lastStich);
        buttons.add(button_clearTable);
        if (isAdmin) {
            JButton button_adminPanel = new JButton("Admin");
            buttons.add(button_adminPanel);
            button_adminPanel.addActionListener(e -> {
                if(admin_panel!=null){
                    admin_panel.dispose();
                }
                createAdminUI();
            });
        }
        tableButtons.add(buttons);
        tableButtons.add(new JLabel());
        tableButtons.add(new JLabel());
        return tableButtons;
    }

    private JPanel createMessageLabelPanel(){
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(serverMessageLabel);
        panel.add(gameMessageLabel);
        return panel;
    }


    private void updateTable(){
        tableLable.revalidate();
        tableLable.repaint();
        table.revalidate();
        table.repaint();
        layeredPane.revalidate();
        layeredPane.repaint();
    }


}
