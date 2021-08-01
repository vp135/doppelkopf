import base.*;
import base.messages.*;
import base.messages.admin.AbortGame;
import base.messages.admin.SetAdmin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.*;

public abstract class BaseClient implements IInputputHandler {

    protected Logger log;

    protected JFrame mainFrame= null;
    protected JFrame letzterStich;
    protected JLayeredPane layeredPane;
    protected JPanel panel;
    protected JPanel mainPanel;
    protected JPanel table;
    protected JPanel controlPanel;
    protected JPanel hud;
    protected JPanel bottomPanel;
    protected JLabel serverMessageLabel;
    protected JLabel gameMessageLabel;
    protected JLabel userLabel_1;
    protected JLabel userLabel_2;
    protected JLabel userLabel_3;
    protected JLabel userLabel_4;
    protected JLabel tableLable;
    protected Map<BaseCard,JLabel> labelMap;
    protected Map<JLabel,BaseCard> cardMap;
    protected ArrayList<JButton> buttonList;
    protected Graphics playArea;

    protected JLayeredPane overLayer;

    protected JButton sendCardsButton;

    protected List<String> serverMessages;


    //Cards

    private HashMap<String,BufferedImage> rawImages = new HashMap<>();
    private HashMap<String,ImageIcon> rawIcons = new HashMap<>();

    private BufferedImage img;
    public static final double RATIO = 0.67;
    protected final HashMap<String,ImageIcon> cardIcons = new HashMap<>();
    private final HashMap<String,BufferedImage> cardImages = new HashMap<>();
    protected int cardSize;
    private int cardHeight4Hand;


    protected MouseAdapter handCardClickAdapter;
    protected MouseAdapter exchangeCardClickAdapter;
    protected HashMap<Integer, BaseCard> tableStich = new HashMap<>();
    protected boolean wait4Player = false;
    protected boolean selectCards = false;
    protected List<BaseCard> hand;

    protected BaseCard[] exchangeCards;
    protected JLabel[] cLabels;


    protected Configuration c;
    protected ComClient handler;
    protected final List<String> players;
    protected int maxHandCards = 13;
    protected float heightCorrection;

    protected boolean test;
    protected boolean isAdmin;

    protected final Random random = new Random(System.currentTimeMillis());
    protected JPanel middlePanel;
    protected JPanel configPanel;
    private JLabel button_config;

    public BaseClient(ComClient handler, List<String> players, Configuration c) {
        this.handler = handler;
        this.players = players;
        this.c = c;
        log = new Logger(c.connection.name, 4,true);
        serverMessages = new ArrayList<>();
        setGameSpecifics();
        setCardClickAdapter();

    }

    @Override
    public void handleInput(RequestObject input) {
        log.info("received: " +input.getCommand());
        try {
            switch (input.getCommand()) {
                case DisplayMessage.COMMAND:
                    String s = LocalTime.now().toString().split("\\.")[0];
                    serverMessages.add( s +" - "+ input.getParams().get("message").getAsString());
                    displayAllServerMessages();
                    break;
                case SetAdmin.COMMAND:
                    isAdmin = input.getParams().get("isAdmin").getAsBoolean();
                    break;
                case EndClients.COMMAND:
                    System.exit(0);
                    break;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void displayAllServerMessages() {
        StringBuilder s = new StringBuilder("<html>");
        for (int i = serverMessages.size()-1;i>-1;i--){
            s.append(serverMessages.get(i)).append("<br>");
        }
        s.append("</html>");
        serverMessageLabel.setText(s.toString());
    }


    //UI creation functions

    protected void createUI(int state, int posX, int posY, Dimension size, boolean test){
        createGameSpecificButtons();
        createMainFrame(state, posX, posY, size);
        createPlayArea();
        createHUD();
        createControlArea();

        this.test = test;
        if(!test) {
            mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int reply = JOptionPane.showOptionDialog(mainFrame, "Wirklich beenden?", "Beenden?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Ja", "Nein"},
                            null);
                    if (reply == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            });
        }
        else{
            uiTest();
        }
        log.info("listeners added");
        redrawEverything();
        log.info("finished UI creation");

        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                redrawEverything();
            }
        });
    }

    protected void uiTest() {

    }

    /**
     * Creates the Main Window for the Game. The following parameters are used to display the window in a specific way
     * @param state window state  (maximized or not)
     * @param posX x position where the upper left corner will be set to
     * @param posY y position where the upper left corner will be set to
     * @param size dimension of the new Window at startup
     */
    private void createMainFrame(int state, int posX, int posY, Dimension size) {
        log.info("creating UI");
        mainFrame = new JFrame("Doppelkopf/Skat Version "+ Statics.VERSION + " " + c.connection.name );
        mainPanel =new JPanel(new GridBagLayout());
        mainFrame.setExtendedState(state);
        mainFrame.setLocation(posX, posY);
        mainFrame.add(mainPanel);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setSize(size);



        overLayer = new JLayeredPane();
        setComponentSizes(overLayer,new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        layeredPane = new JLayeredPane();
        setComponentSizes(layeredPane,new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        cardSize = mainFrame.getHeight()/30*8;
    }

    /**
     * Creates the area where played cards will be shown
     */
    protected void createPlayArea() {
        log.info("creating play area");
        table = new JPanel();
        setComponentSizes(table, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        table.setBackground(new Color(0,0,0,0));
        //table.setBackground(Color.RED);
        img = new BufferedImage(table.getWidth(), table.getWidth(), BufferedImage.TYPE_INT_ARGB);
        playArea = img.getGraphics();
        playArea.drawImage(img, 0, 0, table.getHeight(), table.getWidth(), null);
        tableLable = new JLabel(new ImageIcon(img));
        table.add(tableLable);
        //table.setBackground(Color.BLACK);
        layeredPane.add(table,0);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(configPanel!=null
                        && (e.getX()>configPanel.getSize().width
                        || e.getY()>configPanel.getSize().height)) {
                    configPanel.setVisible(false);
                    button_config.setEnabled(true);
                    overLayer.moveToFront(layeredPane);
                    c.saveConfig();
                }
            }
        });
    }

    /**
     * Creates overlaying UI where game info will be shown(Usernames, game messages)
     */
    protected void createHUD() {
        log.info("creating hud");
        hud = new JPanel(new GridLayout(3,3));
        hud.setBackground(new Color(0,0,0,0));
        //hud.setBackground(Color.RED);



        userLabel_1= new JLabel();
        userLabel_1.setOpaque(true);
        userLabel_1.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel_1.setBackground(new Color(0,0,0,0));
        userLabel_2 = new JLabel();
        userLabel_2.setOpaque(true);
        userLabel_2.setVerticalAlignment(SwingConstants.TOP);
        userLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel_2.setBackground(new Color(0,0,0,0));
        userLabel_3 = new JLabel();
        userLabel_3.setOpaque(true);
        userLabel_3.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel_3.setBackground(new Color(0,0,0,0));
        userLabel_4 = new JLabel();
        userLabel_4.setOpaque(true);
        userLabel_4.setVerticalAlignment(SwingConstants.BOTTOM);
        userLabel_4.setHorizontalAlignment(SwingConstants.LEFT);
        userLabel_4.setBackground(new Color(0,0,0,0));
        middlePanel = new JPanel(new GridLayout(1,3));
        middlePanel.setBackground(new Color(0,0,0,0));
        //middlePanel.setBackground(Color.CYAN);

        String path = new File(System.getProperty("user.dir") + File.separator+ "resources"+File.separator + "options.png").getAbsolutePath();
        try {
            button_config = new JLabel(new ImageIcon(ImageIO.read(new File(path))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JPanel openPanel = new JPanel(new SpringLayout());
        openPanel.add(button_config);

        openPanel.setBackground(new Color(0,0,0,0));
        button_config.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        button_config.setEnabled(false);
                        createUIConfigPanel();
                        overLayer.add(configPanel, 2);
                        overLayer.moveToFront(configPanel);
                    }
                }
        );

        hud.add(openPanel);
        hud.add(userLabel_2);
        hud.add(new JLabel());
        hud.add(userLabel_1);
        hud.add(middlePanel);
        hud.add(userLabel_3);
        hud.add(new JLabel());
        hud.add(userLabel_4);
        createMessageLabelPanel();
        createUIConfigPanel();

        layeredPane.add(hud,1);
        layeredPane.moveToFront(hud);
    }



    protected void createUIConfigPanel(){
        configPanel = new JPanel(new GridLayout(15,2));
        setComponentSizes(configPanel,new Dimension(600,600));
        configPanel.add(new JLabel("Kartenwinkel (rechts/links)"));
        JTextField angle24Field = new JTextField();
        angle24Field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try{
                    c.ui.angle24 = Integer.parseInt(((JTextField) e.getComponent()).getText());
                }
                catch (Exception ex){
                    ((JTextField) e.getComponent()).setText(String.valueOf(c.ui.angle24));
                }
            }
        });
        configPanel.add(angle24Field);
        configPanel.add(new JLabel("Kartenwinkel (oben/unten)"));
        JTextField angle13Field = new JTextField();
        angle13Field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try{
                    c.ui.angle13 = Integer.parseInt(((JTextField) e.getComponent()).getText());
                }
                catch (Exception ex){
                    ((JTextField) e.getComponent()).setText(String.valueOf(c.ui.angle13));
                }
            }
        });
        configPanel.add(angle13Field);
        configPanel.add(new JLabel("max Abweichung des Winkels"));
        JTextField angleVariationField = new JTextField();
        angleVariationField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try{
                    c.ui.angleVariation = Integer.parseInt(((JTextField) e.getComponent()).getText());
                }
                catch (Exception ex){
                    ((JTextField) e.getComponent()).setText(String.valueOf(c.ui.angleVariation));
                }
            }
        });
        configPanel.add(angleVariationField);
        configPanel.add(new JLabel("Relativer Abstand der Karten zur Tischmitte"));
        JTextField distanceField = new JTextField();
        distanceField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try{
                    c.ui.distanceFromCenter = Integer.parseInt(((JTextField) e.getComponent()).getText());
                }
                catch (Exception ex){
                    ((JTextField) e.getComponent()).setText(String.valueOf(c.ui.distanceFromCenter));
                }
            }
        });
        configPanel.add(distanceField);
        configPanel.add(new JLabel("max relative Abweichung des Abstands"));
        JTextField distanceVariationField = new JTextField();
        distanceVariationField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try{
                    c.ui.distanceVariation = Integer.parseInt(((JTextField) e.getComponent()).getText());
                }
                catch (Exception ex){
                    ((JTextField) e.getComponent()).setText(String.valueOf(c.ui.distanceVariation));
                }
            }
        });
        configPanel.add(distanceVariationField);
        JCheckBox repositionCards = new JCheckBox("Karten neu verteilen");
        repositionCards.addActionListener(e -> c.ui.redrawCards=repositionCards.isSelected());
        configPanel.add(repositionCards);

        angle24Field.setText(String.valueOf(c.ui.angle24));
        angle13Field.setText(String.valueOf(c.ui.angle13));
        angleVariationField.setText(String.valueOf(c.ui.angleVariation));
        distanceField.setText(String.valueOf(c.ui.distanceFromCenter));
        distanceVariationField.setText(String.valueOf(c.ui.distanceVariation));
        repositionCards.setSelected(c.ui.redrawCards);

        JButton button_abortGame = new JButton("Spiel abbrechen");
        JButton button_shuffle = new JButton("shuffle");
        JButton button_ackEnddialogs = new JButton("Dialoge quittieren");
        JButton button_endClients = new JButton("Clients beenden");

        JButton button_lastStich = new JButton("letzter Stich");
        JButton button_clearTable = new JButton("Tisch leeren");
        configPanel.add(new JLabel());

        if(isAdmin) {
            configPanel.add(new JSeparator());
            configPanel.add(new JSeparator());
            configPanel.add(button_abortGame);
            configPanel.add(button_shuffle);
            configPanel.add(button_ackEnddialogs);
            configPanel.add(button_endClients);
        }
        configPanel.add(new JSeparator());
        configPanel.add(new JSeparator());
        configPanel.add(button_lastStich);
        configPanel.add(button_clearTable);


        button_abortGame.addActionListener(e -> handler.queueOutMessage(new AbortGame()));
        button_shuffle.addActionListener(e -> handler.queueOutMessage(new AdminRequest(Statics.ADMINREQUESTS.SHUFFLE)));
        button_ackEnddialogs.addActionListener(e -> handler.queueOutMessage(new AdminRequest(Statics.ADMINREQUESTS.ACKNOWLEDGE)));
        button_endClients.addActionListener(e -> handler.queueOutMessage(new AdminRequest(Statics.ADMINREQUESTS.END_CLIENTS)));
        button_lastStich.addActionListener(e -> handler.queueOutMessage(new CurrentStich(new HashMap<>(), players.indexOf(c.connection.name), true)));
        button_clearTable.addActionListener(e -> clearPlayArea());
        configPanel.setBorder(new LineBorder(Color.WHITE,3));
    }

    /**
     * Creates area where the player can interact with the game and other players (own cards, sort cards, select game to play etc.)
     */
    private void createControlArea() {
        panel = new JPanel(new GridLayout(1,13));

        log.info("setting components");
        GridBagConstraints cbc = new GridBagConstraints();

        cbc.gridx=0;
        cbc.gridy=0;
        cbc.gridheight=20;
        cbc.gridwidth=1;
        cbc.weighty=20;
        cbc.anchor = GridBagConstraints.NORTH;
        overLayer.add(layeredPane);
        mainPanel.add(overLayer,cbc);
        bottomPanel = new JPanel(new BorderLayout());
        setComponentSizes(bottomPanel,new Dimension(mainFrame.getWidth(),mainFrame.getHeight()/30*9));
        cbc.gridx=0;
        cbc.gridy=21;
        cbc.weighty=9;
        cbc.gridheight=9;
        cbc.gridwidth=1;
        mainPanel.add(bottomPanel,cbc);


        controlPanel = new JPanel(new GridLayout(1,6));
        bottomPanel.add(controlPanel,BorderLayout.NORTH);
        bottomPanel.add(panel);
        controlPanel.setVisible(false);

        log.info("components set");
    }



    /**
     * Creates UI where game messages can be displayed
     */
    protected void createMessageLabelPanel() {
        serverMessageLabel = new JLabel("");
        serverMessageLabel.setOpaque(true);
        serverMessageLabel.setBorder(new LineBorder(Color.BLACK));
        gameMessageLabel = new JLabel("");
        gameMessageLabel.setBackground(Color.GREEN);
        JPanel messagesPanel = new JPanel(new GridLayout(2, 1));
        JScrollPane scrollPane = new JScrollPane(serverMessageLabel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));
        scrollPane.setOpaque(true);
        scrollPane.setBorder(new LineBorder(Color.BLACK));
        scrollPane.setAutoscrolls(true);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.repaint();
        messagesPanel.add(scrollPane);
        messagesPanel.add(gameMessageLabel);
        hud.add(messagesPanel);
    }

    // abstract game specifc methods

    protected abstract void createGameSpecificButtons();

    protected abstract void setGameSpecificButtons(List<BaseCard> hand);

    protected abstract void setGameSpecifics();

    protected abstract void setCardClickAdapter();

    /**
     * Draws card onto the play area.
     * @param card defines the card which will be drawn
     * @param pos sets position where the card will be placed/which player used the card
     * @param canvasHeight height of the play area
     * @param canvasWidth widht of the play area
     */
    protected void drawCard2Position(BaseCard card, int pos, int canvasHeight, int canvasWidth){
        AffineTransform at;
        int distFromCenter = cardSize*c.ui.distanceFromCenter/100;
        int theta = c.ui.angleVariation - random.nextInt(c.ui.angleVariation*2 + 1);
        int distVar = distFromCenter +  c.ui.distanceVariation - random.nextInt(c.ui.distanceVariation*2 + 1);
        BufferedImage img = cardImages.get(card.farbe+card.value);
        int halfHeight = canvasHeight/2;
        int halfWidth = canvasWidth/2;
        int anchorY = (int) (halfHeight*heightCorrection);
        int anchorX = halfWidth;
        switch (pos){
            case 0:
                anchorY= halfHeight + distVar;
                theta += c.ui.angle13;
                break;
            case 1:
                anchorX = halfWidth - distVar;
                theta += c.ui.angle24;
                break;
            case 2:
                anchorY = halfHeight - distVar;
                theta += c.ui.angle13;
                break;
            case 3:
                anchorX = halfWidth + distVar;
                theta += c.ui.angle24;
                break;
        }
        at = AffineTransform.getRotateInstance(Math.toRadians(theta),anchorX,anchorY);
        at.translate(anchorX-(cardSize*RATIO/2),anchorY-(double)cardSize/2);
        Graphics2D g = (Graphics2D) playArea;
        g.drawImage(img, at, null);
    }



    //UI helper functions

    protected void deselectAllSortButtons(){
        buttonList.forEach(button-> button.setBackground(Color.BLACK));
    }


    protected void createCardButtons(List<BaseCard> cards) {
        panel.removeAll();
        labelMap = new HashMap<>();
        cardMap = new HashMap<>();
        setComponentSizes(panel,new Dimension((int)(cardSize*RATIO*maxHandCards),panel.getHeight()));
        cards.forEach(this::getCardLabel4Hand);
        panel.revalidate();
        panel.repaint();
    }


    protected void createCards() {
        int cardWidth4Hand = panel.getWidth() / maxHandCards;
        cardHeight4Hand = (int) (cardWidth4Hand / RATIO);
        BaseCard.UNIQUE_CARDS.forEach(s -> {
            try {
                double factor = Math.min(cardHeight4Hand, cardSize) * RATIO / (double) rawImages.get(s).getWidth();
                BufferedImage cardImage = new BufferedImage(
                        (int) (Math.min(cardHeight4Hand, cardSize) * RATIO),
                        Math.min(cardHeight4Hand, cardSize),
                        BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                at.scale(factor, factor);
                AffineTransformOp scaleOp =
                        new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                cardImage = scaleOp.filter(rawImages.get(s), cardImage);
                rawIcons.get(s).setImage(cardImage);
                cardIcons.put(s, rawIcons.get(s));

                if (Math.min(cardHeight4Hand, cardSize) == cardHeight4Hand) {
                    factor = cardSize * RATIO / (double) rawImages.get(s).getWidth();
                    cardImage = new BufferedImage(
                            (int) (cardSize * RATIO),
                            cardSize,
                            BufferedImage.TYPE_INT_ARGB);
                    AffineTransform at2 = new AffineTransform();
                    at2.scale(factor, factor);
                    scaleOp = new AffineTransformOp(at2, AffineTransformOp.TYPE_BILINEAR);
                    cardImage = scaleOp.filter(rawImages.get(s), cardImage);
                }
                cardImages.put(s, cardImage);
            } catch (Exception e) {
                log.warn(e.toString());
            }
        });
    }


    protected void getCardLabel4Hand(BaseCard card){
        JPanel p = new JPanel();
        JLabel label = new JLabel();
        label.setIcon(cardIcons.get(card.farbe+card.value));
        labelMap.put(card,label);
        cardMap.put(label,card);
        label.addMouseListener(handCardClickAdapter);
        p.add(label);
        panel.add(p);
    }

    protected void redrawEverything() {
        log.info("starting to redraw");
        cardSize = mainFrame.getHeight() / 30 * 8;
        setComponentSizes(layeredPane, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        setComponentSizes(table, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15 * 10));
        setComponentSizes(hud, new Dimension(layeredPane.getWidth() - 15, mainFrame.getHeight() / 15 * 10));
        setComponentSizes(bottomPanel, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 30 * 9));
        setComponentSizes(panel, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 30 * 8));
        setComponentSizes(controlPanel, new Dimension(mainFrame.getWidth(), mainFrame.getHeight() / 15));
        createCards();
        //createCardBase();
        log.info("finished redrawing");
    }

    protected void setComponentSizes(JComponent p, Dimension d){
        p.setPreferredSize(d);
        p.setSize(p.getPreferredSize());
        p.setMinimumSize(p.getPreferredSize());
        p.setMaximumSize(p.getPreferredSize());
    }

    protected void clearPlayArea(){
        img = new BufferedImage(table.getWidth(), table.getWidth(), BufferedImage.TYPE_INT_ARGB);
        playArea = img.getGraphics();
        playArea.drawImage(img, 0, 0, table.getHeight(), table.getWidth(), null);
        tableLable = new JLabel(new ImageIcon(img));
        table.removeAll();
        table.add(tableLable);
        updateTable();
    }

    protected JLabel getCardLabel(BaseCard card){
        int size = 10;
        JLabel label = new JLabel(rawIcons.get(card.toTrimedString()));
        label.setSize(new Dimension((int)mainFrame.getSize().getWidth()/size,110));
        return label;
    }

    protected void updateTable(){
        tableLable.revalidate();
        tableLable.repaint();
        table.revalidate();
        table.repaint();
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    protected void handleCards(RequestObject message) {
        updateTable();
        panel.removeAll();
        createCardButtons(hand);
        setGameSpecificButtons(hand);
        tableStich = new HashMap<>();
        gameMessageLabel.setText("");
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    protected void handleWait4Player(RequestObject message) {
        if (message.getParams().get("player").getAsString().equals(c.connection.name)) {
            gameMessageLabel.setText("Du bist am Zug");
            wait4Player = true;
        } else {
            gameMessageLabel.setText(message.getParams().get("player").getAsString() + " ist am Zug");
        }
    }

    public void setRawCards(HashMap<String, ImageIcon> rawIcons, HashMap<String, BufferedImage> rawImages) {
        this.rawIcons = rawIcons;
        this.rawImages = rawImages;
    }

    protected void moveCard2Hand(BaseCard card) {
        hand.add(card);
        getCardLabel4Hand(card);
    }

    protected  void moveCard2Exchange(BaseCard card){
        moveCard2Exchange(card,false);
    }

    protected void moveCard2Exchange(BaseCard card, boolean force) {
        if(hand.size()>10 || force) {
            for (int i = 0; i < exchangeCards.length; i++) {
                try {
                    if (exchangeCards[i] == null) {
                        exchangeCards[i] = card;
                        cLabels[i].setIcon(cardIcons.get(card.farbe + card.value));
                        cLabels[i].setVisible(true);
                        middlePanel.add(cLabels[i]);
                        break;
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            hand.remove(card);
            createCardButtons(hand);
        }
    }
    //

}
