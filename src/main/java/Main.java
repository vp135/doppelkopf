import base.*;
import base.doko.DokoConfig;
import base.doko.DokoServer;
import base.messages.*;
import base.skat.SkatConfig;
import base.skat.SkatServer;
import com.google.gson.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Main implements IInputputHandler{

    private final Logger log = new Logger("Client",4);

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
    private boolean isAdmin = false;


    private ComClient comClient;
    private JComboBox<Statics.game> gameList;
    private JPanel configPanel;
    private String selectionPlayer ="";

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
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
        playerList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(isAdmin) {
                    switch (e.getKeyCode()) {
                        case 33://page up
                        case 38://arrow up
                            if (playerList.getSelectedIndex() > 0) {
                                String playerDown = players.get(playerList.getSelectedIndex() - 1);
                                String playerUp = players.get(playerList.getSelectedIndex());
                                players.set(playerList.getSelectedIndex() - 1, playerUp);
                                players.set(playerList.getSelectedIndex(), playerDown);
                                selectionPlayer = playerUp;
                                comClient.queueOutMessage(MessagePlayerList.playerOrderChanged(players));
                            }
                            break;
                        case 34://page down
                        case 40://arrow down
                            if (playerList.getSelectedIndex() < players.size() - 1 && playerList.getSelectedIndex() > -1) {
                                String playerDown = players.get(playerList.getSelectedIndex() + 1);
                                String playerUp = players.get(playerList.getSelectedIndex());
                                players.set(playerList.getSelectedIndex() + 1, playerUp);
                                players.set(playerList.getSelectedIndex(), playerDown);
                                selectionPlayer = playerUp;
                                comClient.queueOutMessage(MessagePlayerList.playerOrderChanged(players));
                            }
                            break;
                    }
                }
            }
        });


        rightPanel.add(playerList);
        panel.add(rightPanel);
        inputs.add(create);
        inputs.add(join);



        if (c != null) {
            playername.setText(c.connection.name);
            hostname.setText(c.connection.server);
            port.setText(String.valueOf(c.connection.port));

        }

        create.addActionListener(e -> {
            isAdmin = true;
            name = playername.getText();
            c.connection.name = name;
            c.connection.server = hostname.getText();
            c.connection.port = Integer.parseInt(port.getText());
            if (!playername.getText().trim().equals("") && !port.getText().trim().equals("")) {
                server = new BaseServer(c, new ComServer(Integer.parseInt(port.getText()),c.logLevel));
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
                    //join.setEnabled(false);
                    comClient = new ComClient(hostname.getText(), Integer.parseInt(port.getText()), this, name, c.logLevel);
                    comClient.queueOutMessage(new MessageGetVersion(name, Statics.VERSION));
                    comClient.start();
                    inputs.add(gameList);
                    inputs.add(start);
                    createConfigPanel();
                    rightPanel.add(configPanel);
                    gameList.setSelectedItem(Statics.game.valueOf(c.lastGame));
                    setConfigMenu();


                    addNPC();
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
                    comClient = new ComClient(hostname.getText(),Integer.parseInt(port.getText()), this,name,c.logLevel);
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
                        comClient.queueOutMessage(new MessageGetVersion(name,Statics.VERSION));
                    } else {
                        comClient.clearQueue();
                        join.setText("beitreten");
                        log.info("beitreten");
                    }
                }
            }).start();

        });
        start.addActionListener(e ->{
            startGameServer((Statics.game) Objects.requireNonNull(gameList.getSelectedItem()));
        });
        createJoinFrame.pack();
        createJoinFrame.add(panel);
        createJoinFrame.setVisible(true);
        createJoinFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        createJoinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void addNPC() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String[] fakeNames = new String[]{"Steve","Bob","Kinsley"};
            for(int i = 0; i<c.comPlayer&&i<3;i++){
                new FakeClient(c,fakeNames[i]);
            }
        }).start();

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


    public void handleInput(Message message) {
        switch (message.getCommand()) {
            case MessagePlayerList.IN_LOBBY:
            case MessagePlayerList.CHANGE_ORDER:
                handlePlayersInLobby(message);
                break;
            case MessageStartGame.COMMAND:
                handleStart(message);
                break;
            case MessageGetVersion.COMMAND:
                handleGetVersion(message);
                break;
        }
    }


    private void handleGetVersion(Message message) {
        try {
            MessageGetVersion messageGetVersion = new MessageGetVersion(message);
            if (!Statics.VERSION.equals(messageGetVersion.getVersion())) {
                JOptionPane.showMessageDialog(createJoinFrame,
                        "Version(Server): " + messageGetVersion.getVersion() + "\n" +
                                "Version(lokal): " + Statics.VERSION);
            }
        }catch (Exception ex){
            log.error(ex.toString());
        }
    }


    private void handlePlayersInLobby(Message message) {
        try {
            log.info("draw playerlist");
            MessagePlayerList messagePlayersInLobby = new MessagePlayerList(message);
            players = messagePlayersInLobby.getPlayerNamesList();
            playerList.removeAll();
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addAll(players);
            playerList.setModel(model);
            if (!selectionPlayer.equals("")) {
                playerList.setSelectedValue(selectionPlayer, true);
            }
            if (players.size() > 0) {
                start.setEnabled(true);
            }
            join.setEnabled(false);
            join.setText("verbunden");
        }
        catch (Exception ex){
            log.error(ex.toString());
        }
    }


    private void handleStart(Message message) {
        try {
            MessageStartGame messageStartGame = new MessageStartGame(message);
            if (client == null || client.mainFrame == null) {
                while (!ready) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                c.lastGame = messageStartGame.getGame().name();
                c.saveConfig();
                switch (messageStartGame.getGame()) {
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
                        createJoinFrame.getSize());
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
        catch (Exception ex){
            log.error(ex.toString());
        }
    }


    HashMap<String, BufferedImage> rawImages = new HashMap<>();
    HashMap<String,ImageIcon> rawIcons = new HashMap<>();
    boolean ready;

    public void createRawCardMaps(){
        new Thread(() ->{
            Card.UNIQUE_CARDS.forEach(s -> {
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
                setConfigMenu();
            }
        });


    }

    private void setConfigMenu() {
        switch ((Statics.game) gameList.getSelectedItem()) {
            case DOKO:
                configPanel.removeAll();
                configPanel.add(createDokoConfigPanel(c.doko),BorderLayout.CENTER);
                break;
            case SKAT:
                configPanel.removeAll();
                configPanel.add(createSkatConfigPanel(c.skat),BorderLayout.CENTER);
                break;
        }

        configPanel.revalidate();
        configPanel.repaint();
    }

    private Component createSkatConfigPanel(SkatConfig skat) {
        JPanel panel = new JPanel();
        Gson gson = new GsonBuilder().create();
        JsonObject jObject=  gson.toJsonTree(skat).getAsJsonObject();
        panel.add(createConfigPanelFromJson("skat" ,jObject));
        return panel;
    }

    private JPanel createDokoConfigPanel(DokoConfig doko) {
        JPanel panel = new JPanel();
        Gson gson = new GsonBuilder().create();
        JsonObject jObject=  gson.toJsonTree(doko).getAsJsonObject();
        panel.add(createConfigPanelFromJson("doko" ,jObject));
        return panel;
    }

    private JComponent createConfigPanelFromJson(String name, JsonElement element){

        if(element.isJsonPrimitive()){
            JsonPrimitive p = element.getAsJsonPrimitive();
            if(p.isBoolean()){
                JCheckBox cb = new JCheckBox(name,p.getAsBoolean());
                cb.addActionListener(e ->  createConfigFromJPanel());
                return cb;
            }
            else if(p.isNumber()){
                JPanel pan = new JPanel(new GridLayout(1,2));
                pan.add(new JLabel(name));
                JTextField textField = new JTextField(p.getAsNumber().toString());
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        createConfigFromJPanel();
                    }
                });
                pan.add(textField);
                return pan;
            }
        }
        else if(element.isJsonObject()){
            JsonObject o = element.getAsJsonObject();
            JPanel panel = new JPanel();
            BoxLayout b = new BoxLayout(panel,BoxLayout.PAGE_AXIS);
            panel.setLayout(b);
            panel.add(new JLabel(name));
            for (String s: o.keySet()) {
                JsonElement e = o.get(s);
                panel.add(createConfigPanelFromJson(s,e));
            }
            return panel;
        }
        return new JPanel();
    }

    private void createConfigFromJPanel() {
        for (Component comp: configPanel.getComponents()) {
            JsonObject jsonObject = getElementFromComponent((JComponent) comp).getAsJsonObject();
            if(jsonObject.has("doko")) {
                c.doko = new GsonBuilder().create().fromJson(jsonObject.getAsJsonObject("doko"), DokoConfig.class);
            }
            if(jsonObject.has("skat")){
                c.skat = new GsonBuilder().create().fromJson(jsonObject.getAsJsonObject("skat"), SkatConfig.class);
            }
            c.saveConfig();
        }
    }

    private JsonElement getElementFromComponent(JComponent component){
        JsonElement element;
        JsonObject jsonObject = new JsonObject();
        for (Component component1: component.getComponents()) {
            if(component1 instanceof JCheckBox){
                JCheckBox checkBox = (JCheckBox) component1;
                jsonObject.add(checkBox.getText(),new JsonPrimitive(checkBox.isSelected()));
            }
            else if(component1 instanceof JPanel){
                JPanel panel = (JPanel) component1;
                if(panel.getComponents().length==2
                        && panel.getComponents()[0] instanceof JLabel
                        && panel.getComponents()[1] instanceof JTextField){
                    jsonObject.add(((JLabel) panel.getComponents()[0]).getText(),
                            new JsonPrimitive(((JTextField) panel.getComponents()[1]).getText()));
                }
                else {
                    jsonObject.add(
                            ((JLabel)panel.getComponent(0)).getText(),
                            getElementFromComponent(panel));
                }
            }
        }

        element = jsonObject;

        return element;
    }

}
