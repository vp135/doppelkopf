import base.*;
import base.messages.GetVersion;
import base.messages.PlayersInLobby;
import base.messages.RequestObject;
import base.messages.StartGame;
import com.google.gson.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Main implements IInputputHandler{

    private final Logger log = new Logger(this.getClass().getName(),1);

    static Main m;

    private String name;
    private List<String> players = new ArrayList<>();
    private final JList<String> playerList = new JList<>();
    private BaseServer server;
    private BaseClient client = null;
    private JButton start;
    private JButton join;
    private JFrame createJoinFrame;
    private Configuration c;


    private ComClient comClient;
    private JComboBox<Statics.game> gameList;
    private JPanel configPanel;

    public static void main(String[] args) {
        m = new Main();
    }


    private void modifyUIManager() {
        UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("Label.background",Color.BLACK);
        UIManager.put("Label.foreground",Color.WHITE);
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("TextField.background",new Color(70,70,70));
        UIManager.put("TextField.foreground",Color.WHITE);
        UIManager.put("Panel.background",Color.BLACK);
        UIManager.put("TextArea.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("TextArea.background",Color.BLACK);
        UIManager.put("TextArea.foreground",Color.WHITE);
        UIManager.put("CheckBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("CheckBox.background",Color.BLACK);
        UIManager.put("CheckBox.foreground",Color.WHITE);
        UIManager.put("ComboBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("ComboBox.background",Color.BLACK);
        UIManager.put("ComboBox.foreground",Color.WHITE);
        UIManager.put("OptionPane.messageForeground",Color.WHITE);
        //This shit does not work for some fucking reason
        UIManager.put("List.font", new FontUIResource(new Font("Dialog", Font.BOLD, c.ui.textsize)));
        UIManager.put("List.background",Color.BLACK);
        UIManager.put("List.foreground",Color.WHITE);
        UIManager.put("List.selectionBackground",Color.GREEN);
        UIManager.put("List.selectionForeground",Color.BLUE);
        //

        Toolkit.getDefaultToolkit().setDynamicLayout(false);
    }

    public Main(){
        createOrJoin();
        createRawCardMaps();
    }

    public void createOrJoin() {
        c = Configuration.fromFile();
        log.setLoglevel(c.logLevel);
        modifyUIManager();
        createJoinFrame = new JFrame();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JPanel inputs = new JPanel(new GridLayout(5, 2));
        JPanel rightPanel = new JPanel(new GridLayout(3,1));


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
        playerList.setFont(playerList.getFont().deriveFont((float)c.ui.textsize));


        rightPanel.add(playerList);
        panel.add(rightPanel);
        inputs.add(create);
        inputs.add(join);



        if (c != null) {
            playername.setText(c.connection.name);
            hostname.setText(c.connection.server);
            port.setText(String.valueOf(c.connection.port));
            gameList.setSelectedItem(Statics.game.valueOf(c.lastGame));
        }

        create.addActionListener(e -> {
            name = playername.getText();
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
                server = new BaseServer(c, new ComServer(Integer.parseInt(port.getText())));
                playerList.addListSelectionListener(p -> {
                    if(server!=null){
                        server.setStartPlayer(playerList.getSelectedIndex());
                    }
                });
                int i = 0;
                int nmbRetries = 15;
                while(!server.comServer.listening && i<nmbRetries) {
                    log.info("not listening");
                    try {
                        i++;
                        Thread.sleep(200);
                    } catch (InterruptedException interruptedException) {
                        log.error(interruptedException.toString());
                    }
                }
                if(server.comServer.listening) {
                    create.setEnabled(false);
                    join.setEnabled(false);
                    comClient = new ComClient(hostname.getText(), Integer.parseInt(port.getText()), this, name);
                    comClient.queueOutMessage(new GetVersion(name, Statics.VERSION));
                    comClient.start();
                    inputs.add(gameList);
                    inputs.add(start);
                    createConfigPanel();
                    rightPanel.add(configPanel);
                }
            }
        });

        join.addActionListener(e -> {
            name = playername.getText();
            new Thread(() -> {
                name = playername.getText().trim();
                if (!name.equals("")) {
                    c.connection.name = name;
                    c.connection.server = hostname.getText();
                    c.connection.port = Integer.parseInt(port.getText());
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
        server.setAdmin(c.connection.name);
        server.startGame();
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
        if(client==null || client.mainFrame==null) {
            while (!ready) {
                try {
                    System.out.println("not ready yet");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            switch (Statics.game.valueOf(message.getParams().get("game").getAsString())) {
                case DOKO:
                    client = new DokoClient(comClient, players, c);
                    break;
                case SKAT:
                    client = new SkatClient(comClient, players, c);
                    break;
            }
            client.setRawCards(rawIcons, rawImages);
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
    }


    HashMap<String, BufferedImage> rawImages = new HashMap<>();
    HashMap<String,ImageIcon> rawIcons = new HashMap<>();
    boolean ready;

    public void createRawCardMaps(){
        new Thread(() ->{
            BaseCard.UNIQUE_CARDS.forEach(s -> {
                String path = new File(System.getProperty("user.dir") + File.separator+ "resources"+File.separator + s + ".png").getAbsolutePath();
                try {
                    rawImages.put(s, ImageIO.read(new File(path)));
                    rawIcons.put(s, new ImageIcon(ImageIO.read(new File(path))));
                }catch (Exception ex){
                    log.error(ex.toString());
                }
            });
            ready = true;
            log.info( "Cards created");
        }).start();
    }

    public void createConfigPanel(){
        configPanel = new JPanel(new BorderLayout());
        gameList.addItemListener(e ->{
            if(e.getStateChange()== ItemEvent.SELECTED) {
                String text = "";
                switch ((Statics.game) gameList.getSelectedItem()) {
                    case DOKO:
                        configPanel.removeAll();
                        configPanel.add(createGameConfigPanel(c.doko),BorderLayout.CENTER);
                        break;
                    case SKAT:
                        configPanel.removeAll();
                        break;
                }

                configPanel.revalidate();
                configPanel.repaint();
            }
        });


    }

    private JPanel createGameConfigPanel(DokoConfig doko) {
        JPanel panel = new JPanel();
        Gson gson = new GsonBuilder().create();
        JsonObject jObject=  gson.toJsonTree(doko).getAsJsonObject();
        panel.add(test("doko" ,jObject));
        return panel;
    }

    private JComponent test (String name, JsonElement element){

        if(element.isJsonPrimitive()){
            JsonPrimitive p = element.getAsJsonPrimitive();
            if(p.isBoolean()){
                return new JCheckBox(name,p.getAsBoolean());
            }
        }
        else if(element.isJsonObject()){
            JsonObject o = element.getAsJsonObject();
            JPanel panel = new JPanel(new GridLayout(o.keySet().size()+1,2));
            panel.add(new JLabel(name));
            for (String s: o.keySet()) {
                JsonElement e = o.get(s);
                panel.add(test(s,e));
            }
            return panel;
        }
        return new JPanel();
    }

}
