import base.BaseCard;
import base.Logger;
import base.Statics;
import base.messages.StartGame;
import base.messages.GetVersion;
import base.messages.PlayersInLobby;
import base.messages.RequestObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;


public class Main implements IInputputHandler{

    private final Logger log = new Logger(this.getClass().getName(),1);

    static Main m;

    private String name;
    private List<String> players = new ArrayList<>();
    private final JList<String> playerList = new JList<>();
    private BaseServer server;
    private JButton start;
    private JButton join;
    private JFrame createJoinFrame;
    private Configuration c;

    private ComClient comClient;
    private JComboBox<Statics.game> gameList;

    public static void main(String[] args) {
        m = new Main();
    }


    private static void modifyUIManager() {
        UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("Label.background",Color.BLACK);
        UIManager.put("Label.foreground",Color.WHITE);
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("TextField.background",new Color(70,70,70));
        UIManager.put("TextField.foreground",Color.WHITE);
        UIManager.put("Panel.background",Color.BLACK);
        UIManager.put("TextArea.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("TextArea.background",Color.BLACK);
        UIManager.put("TextArea.foreground",Color.WHITE);
        UIManager.put("CheckBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("CheckBox.background",Color.BLACK);
        UIManager.put("CheckBox.foreground",Color.WHITE);
        UIManager.put("ComboBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("ComboBox.background",Color.BLACK);
        UIManager.put("ComboBox.foreground",Color.WHITE);
        UIManager.put("OptionPane.messageForeground",Color.WHITE);
        //This shit does not work for some fucking reason
        UIManager.put("List.font", new FontUIResource(new Font("Dialog", Font.BOLD, 20)));
        UIManager.put("List.background",Color.BLACK);
        UIManager.put("List.foreground",Color.WHITE);
        UIManager.put("List.selectionBackground",Color.GREEN);
        UIManager.put("List.selectionForeground",Color.BLUE);
        //

        Toolkit.getDefaultToolkit().setDynamicLayout(false);
    }

    public Main(){
        modifyUIManager();
        createOrJoin();
        createRawCardMaps();
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
        JCheckBox repositionCards = new JCheckBox("Karten neu verteilen");
        userOptions.add(repositionCards);
        JButton optionsTestButton = new JButton("Einstellungen Testen");
        userOptions.add(optionsTestButton);


        panel.add(inputs);
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
        gameList = new JComboBox<>();
        gameList.setModel(new DefaultComboBoxModel<>(Statics.game.values()));

        //This part is only here because the UIManager does not want to accept my settings for JList
        playerList.setBackground(Color.BLACK);
        playerList.setForeground(Color.WHITE);
        playerList.setFont(playerList.getFont().deriveFont(20f));


        rightPanel.add(playerList);
        rightPanel.add(userOptions);
        panel.add(rightPanel);
        inputs.add(create);
        inputs.add(join);

        optionsTestButton.addActionListener(e -> {
            overrideConfig(angle24Field, angle13Field, angleVariationField, distanceField,
                    distanceVariationField, repositionCards, hostname, port, false);
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
            repositionCards.setSelected(c.redrawCards);

        }

        create.addActionListener(e -> {
            name = playername.getText();
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
                server = new BaseServer(c, new ComServer(Integer.parseInt(port.getText())));
                create.setEnabled(false);
                join.setEnabled(false);
                while(!server.comServer.listening) {
                    log.info("not listening");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        log.error(interruptedException.toString());
                    }
                }
                comClient = new ComClient(hostname.getText(),Integer.parseInt(port.getText()), this,name);
                comClient.queueOutMessage(new GetVersion(name,Statics.VERSION));
                comClient.start();
                inputs.add(gameList);
                inputs.add(start);
            }
            overrideConfig(angle24Field, angle13Field,
                    angleVariationField, distanceField,
                    distanceVariationField, repositionCards,
                    hostname, port, true);
        });

        join.addActionListener(e -> {
            name = playername.getText();
            new Thread(() -> {
                name = playername.getText().trim();
                if (!name.equals("")) {
                    comClient = new ComClient(hostname.getText(),Integer.parseInt(port.getText()), this,name);
                    comClient.start();
                    log.info("verbinde");
                    int dots = 0;
                    long time = System.currentTimeMillis()+5000;
                    while (comClient.wait.get() && System.currentTimeMillis()<time) {
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
                    if (comClient.socketNotNull()){
                        join.setText("verbunden");
                        log.info("verbunden");
                        overrideConfig(angle24Field, angle13Field,
                                angleVariationField, distanceField,
                                distanceVariationField, repositionCards
                                ,hostname, port, true);
                        create.setEnabled(false);
                        comClient.queueOutMessage(new GetVersion(name,Statics.VERSION));
                    } else {
                        comClient.clearQueue();
                        join.setText("beitreten");
                        log.info("beitreten");
                    }
                }
            }).start();

        });
        start.addActionListener(e -> startGameServer((Statics.game) Objects.requireNonNull(gameList.getSelectedItem())));
        createJoinFrame.pack();
        createJoinFrame.add(panel);
        createJoinFrame.setVisible(true);
        createJoinFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        createJoinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void startGameServer(Statics.game game) {
        switch (game){
            case DOKO:
                server = new DokoServer(server);
                break;
            case SKAT:
                server = new SkatServer(server);
                break;
        }
        server.setAdmin(c.name);
        server.startGame();
    }

    private void createOptionsTestFrame() {
        DokoClient client = new DokoClient(null,new ArrayList<>(),c);
        client.createUI(
                createJoinFrame.getExtendedState(),
                createJoinFrame.getX(),
                createJoinFrame.getY(),
                createJoinFrame.getSize(),
                true);
    }

    private void overrideConfig(JTextField angle24Field, JTextField angle13Field,
                                JTextField angleVariationField, JTextField distanceField,
                                JTextField distanceVariationField, JCheckBox repositionCards,
                                JTextField hostname, JTextField port, boolean save) {
        c.name = name;
        c.server = hostname.getText();
        c.port = Integer.parseInt(port.getText());
        c.angle24 = Integer.parseInt(angle24Field.getText());
        c.angle13 = Integer.parseInt(angle13Field.getText());
        c.angleVariation = Integer.parseInt(angleVariationField.getText());
        c.distanceFromCenter = Integer.parseInt(distanceField.getText());
        c.distanceVariation = Integer.parseInt(distanceVariationField.getText());
        c.redrawCards = repositionCards.isSelected();
        if(save) {
            c.saveConfig();
        }
    }


    public void handleInput(RequestObject message) {
        log.info("received: " +message.getCommand());
        switch (message.getCommand()) {
            case PlayersInLobby.COMMAND: {
                handlePlayersInLobby(message);
                break;
            }
            case StartGame.COMMAND: {
                handleStart(message);
                break;
            }
            case GetVersion.COMMAND: {
                handleGetVersion(message);
                break;
            }
        }
    }


    private void handleGetVersion(RequestObject message) {
        if (!Statics.VERSION.equals(message.getParams().get("version").getAsString())) {
            JOptionPane.showMessageDialog(createJoinFrame,
                    "Version(Server): " + message.getParams().get("version").getAsString() + "\n" +
                            "Version(lokal): " + Statics.VERSION);
        }
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
        if (players.size() > 0) {
            start.setEnabled(true);
        }
        join.setEnabled(false);
        join.setText("verbunden");
    }

    private void handleStart(RequestObject message) {
        while(!ready){
            try {
                System.out.println("not ready yet");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BaseClient client = null;
        switch (Statics.game.valueOf(message.getParams().get("game").getAsString())){
            case DOKO:
                client = new DokoClient(comClient,players,c);
                break;
            case SKAT:
                client = new SkatClient(comClient,players,c);
                break;
        }
        client.setRawCards(rawIcons,rawImages);
        comClient.setClient(client);
        client.createUI(
                createJoinFrame.getExtendedState(),
                createJoinFrame.getX(),
                createJoinFrame.getY(),
                createJoinFrame.getSize(),
                false);
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            createJoinFrame.setVisible(false);
            createJoinFrame.dispose();
            log.info("disposed of lobby frame");
        }).start();
    }


    HashMap<String, BufferedImage> rawImages = new HashMap<>();
    HashMap<String,ImageIcon> rawIcons = new HashMap<>();
    boolean ready;

    public void createRawCardMaps(){
        new Thread(() ->{
            BaseCard.UNIQUE_CARDS.forEach(s -> {
                String path = System.getProperty("user.dir") + "\\resources\\" + s + ".PNG";
                try {
                    rawImages.put(s, ImageIO.read(new File(path)));
                    rawIcons.put(s, new ImageIcon(ImageIO.read(new File(path))));
                }catch (Exception ex){
                    log.error(ex.toString());
                }
            });
            ready = true;
            System.out.println("ready");
        }).start();
    }
}
