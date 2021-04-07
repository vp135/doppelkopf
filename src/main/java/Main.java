import base.Logger;
import base.doko.messages.StartGame;
import base.messages.GetVersion;
import base.messages.PlayersInLobby;
import base.messages.RequestObject;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main implements IInputputHandler{

    private final Logger log = new Logger(this.getClass().getName(),1);

    static Main m;


    private String name;
    private List<String> players = new ArrayList<>();
    private final JList<String> playerList = new JList<>();
    private DokoServer dokoServer;
    private JButton start;
    private JButton join;
    private JFrame createJoinFrame;
    private int port;
    private String hostname;

    private Configuration c;

    private final ComHandler handler;
    private boolean isAdmin;

    public static void main(String[] args) {
        m = new Main();
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
        UIManager.put("CheckBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, 15)));
        UIManager.put("CheckBox.background",Color.BLACK);
        UIManager.put("CheckBox.foreground",Color.WHITE);
        UIManager.put("OptionPane.messageForeground",Color.WHITE);
    }

    public Main(){
        modifyUIManager();
        handler = new ComHandler(hostname,port, this);
        createOrJoin();
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
            handler.setName(name);
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
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
                handler.openTCPConnection("127.0.0.1", Integer.parseInt(port.getText()));
                handler.queueOutMessage(new GetVersion(name,DokoServer.VERSION));
                inputs.add(start);
            } else if (!port.getText().trim().equals("")) {
                dokoServer = new DokoServer(Integer.parseInt(port.getText()),c);
                inputs.add(start);
            }
            overrideConfig(angle24Field, angle13Field,
                    angleVariationField, distanceField,
                    distanceVariationField, repositionCards,
                    hostname, port, true);
        });

        join.addActionListener(e -> {
            name = playername.getText();
            handler.setName(name);
            this.port = Integer.parseInt(port.getText());
            new Thread(() -> {
                name = playername.getText().trim();
                if (!name.equals("")) {
                    handler.openTCPConnection(hostname.getText(), Integer.parseInt(port.getText()));
                    log.info("verbinde");
                    int dots = 0;
                    long time = System.currentTimeMillis()+5000;
                    while (handler.wait.get() && System.currentTimeMillis()<time) {
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
                    if (handler.socketNotNull()){
                        join.setText("verbunden");
                        log.info("verbunden");
                        overrideConfig(angle24Field, angle13Field,
                                angleVariationField, distanceField,
                                distanceVariationField, repositionCards
                                ,hostname, port, true);
                        create.setEnabled(false);
                        handler.queueOutMessage(new GetVersion(name,DokoServer.VERSION));
                    } else {
                        handler.clearQueue();
                        join.setText("beitreten");
                        log.info("beitreten");
                    }
                }
            }).start();

        });
        start.addActionListener(e -> {
            isAdmin = true;
            dokoServer.startGame(Math.max(playerList.getSelectedIndex(), 0));
        });
        createJoinFrame.pack();
        createJoinFrame.add(panel);
        createJoinFrame.setVisible(true);
        createJoinFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        createJoinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void createOptionsTestFrame() {
        Doppelkopf_client client = new Doppelkopf_client(null,new ArrayList<>(),c);
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


    public void handleInput(String serverReply) {
        RequestObject message = RequestObject.fromString(serverReply);
        log.info("received: " +message.getCommand());
        switch (message.getCommand()) {
            case PlayersInLobby.COMMAND: {
                handlePlayersInLobby(message);
                break;
            }
            case StartGame.COMMAND: {
                handleStart();
                break;
            }
            case GetVersion.COMMAND: {
                handleGetVersion(message);
                break;
            }
        }
    }


    private void handleGetVersion(RequestObject message) {
        if (!DokoServer.VERSION.equals(message.getParams().get("version").getAsString())) {
            JOptionPane.showMessageDialog(createJoinFrame,
                    "Version(Server): " + message.getParams().get("version").getAsString() + "\n" +
                            "Version(lokal): " + DokoServer.VERSION);
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

    private void handleStart() {
        Doppelkopf_client client = new Doppelkopf_client(handler,players,c);
        handler.setHandler(client);
        client.createUI(
                createJoinFrame.getExtendedState(),
                createJoinFrame.getX(),
                createJoinFrame.getY(),
                createJoinFrame.getSize(),
                false);
        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            createJoinFrame.setVisible(false);
            createJoinFrame.dispose();

        }).start();

        log.info("disposed of lobby frame");
    }
}
