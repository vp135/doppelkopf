import base.BaseCard;
import base.Logger;
import base.Statics;
import base.messages.AbortGame;
import base.messages.CurrentStich;
import base.messages.DisplayMessage;
import base.messages.RequestObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.*;

public abstract class BaseClient implements IInputputHandler {

    protected final static Logger log = new Logger("Client",4,true);

    protected JFrame mainFrame;
    protected JFrame letzterStich;
    protected JFrame adminFrame;
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
    protected List<JLabel> cardLabels2Send = new ArrayList<>();
    protected Map<BaseCard,JLabel> labelMap;
    protected Map<JLabel,BaseCard> cardMap;
    protected ArrayList<JButton> buttonList;
    protected Graphics playArea;

    protected JButton sendCardsButton;


    //Cards

    private HashMap<String,BufferedImage> rawImages = new HashMap<>();
    private HashMap<String,ImageIcon> rawIcons = new HashMap<>();

    private BufferedImage img;
    public static final double RATIO = 0.67;
    protected final HashMap<String,ImageIcon> cardIcons = new HashMap<>();
    private final HashMap<String,BufferedImage> cardImages = new HashMap<>();
    protected int cardSize;
    private int cardWidth4Hand;
    private int cardHeight4Hand;


    protected MouseAdapter cardClickAdapter;
    protected HashMap<Integer, BaseCard> tableStich = new HashMap<>();
    protected boolean wait4Player = false;
    protected boolean selectCards = false;
    protected ArrayList<BaseCard> cards2Send = new ArrayList<>();
    protected List<BaseCard> hand;

    protected Configuration c;
    protected ComClient handler;
    protected final List<String> players;
    protected int maxHandCards = 13;
    protected float heightCorrection;

    protected boolean test;
    protected boolean isAdmin;

    protected final Random random = new Random(System.currentTimeMillis());
    protected JPanel middlePanel;

    public BaseClient(ComClient handler, List<String> players, Configuration c) {
        this.handler = handler;
        this.players = players;
        this.c = c;
        setGameSpecifics();
        setCardClickAdapter();
    }

    @Override
    public void handleInput(RequestObject input) {
        log.info("received: " +input.getCommand());
        try {
            switch (input.getCommand()) {
                case DisplayMessage.COMMAND: {
                    serverMessageLabel.setText(input.getParams().get("message").getAsString());
                    break;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //UI creation functions

    protected void createUI(int state, int posX, int posY, Dimension size, boolean test){
        //createRawCardMaps();
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
        mainFrame = new JFrame("Doppelkopf/Skat Version "+ Statics.VERSION + " " + c.name );
        mainPanel =new JPanel(new GridBagLayout());
        mainFrame.setExtendedState(state);
        mainFrame.setLocation(posX, posY);
        mainFrame.add(mainPanel);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setSize(size);

        serverMessageLabel = new JLabel("");
        gameMessageLabel = new JLabel("");
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
        img = new BufferedImage(table.getWidth(), table.getWidth(), BufferedImage.TYPE_INT_ARGB);
        playArea = img.getGraphics();
        playArea.drawImage(img, 0, 0, table.getHeight(), table.getWidth(), null);
        tableLable = new JLabel(new ImageIcon(img));
        table.add(tableLable);
        layeredPane.add(table,1);
    }

    /**
     * Creates overlaying UI where game info will be shown(Usernames, game messages)
     */
    protected void createHUD() {
        log.info("creating hud");
        hud = new JPanel(new GridLayout(3,3));
        //setComponentSizes(hud, new Dimension(layeredPane.getWidth()-15, mainFrame.getHeight() / 15 * 10));
        hud.setBackground(new Color(0,0,0,0));



        userLabel_1= new JLabel();
        userLabel_1.setOpaque(true);
        userLabel_1.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel_2 = new JLabel();
        userLabel_2.setOpaque(true);
        userLabel_2.setVerticalAlignment(SwingConstants.TOP);
        userLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel_3 = new JLabel();
        userLabel_3.setOpaque(true);
        userLabel_3.setVerticalAlignment(SwingConstants.CENTER);
        userLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel_4 = new JLabel();
        userLabel_4.setOpaque(true);
        userLabel_4.setVerticalAlignment(SwingConstants.BOTTOM);
        userLabel_4.setHorizontalAlignment(SwingConstants.LEFT);
        middlePanel = new JPanel(new GridLayout(1,3));


        hud.add(new JLabel());
        hud.add(userLabel_2);
        hud.add(new JLabel());
        hud.add(userLabel_1);
        hud.add(middlePanel);
        hud.add(userLabel_3);
        hud.add(createControlButtonPanel());
        hud.add(userLabel_4);
        hud.add(createMessageLabelPanel());

        layeredPane.add(hud,2);
    }

    /**
     * Creates area where the player can interact with the game and other players (own cards, sort cards, select game to play etc.)
     */
    private void createControlArea() {
        panel = new JPanel(new GridLayout(1,13));

        log.info("setting components");
        GridBagConstraints c = new GridBagConstraints();

        c.gridx=0;
        c.gridy=0;
        c.gridheight=20;
        c.gridwidth=1;
        c.weighty=20;
        c.anchor = GridBagConstraints.NORTH;
        mainPanel.add(layeredPane,c);
        bottomPanel = new JPanel(new BorderLayout());
        setComponentSizes(bottomPanel,new Dimension(mainFrame.getWidth(),mainFrame.getHeight()/30*9));
        c.gridx=0;
        c.gridy=21;
        c.weighty=9;
        c.gridheight=9;
        c.gridwidth=1;
        mainPanel.add(bottomPanel,c);


        controlPanel = new JPanel(new GridLayout(1,6));
        bottomPanel.add(controlPanel,BorderLayout.NORTH);
        bottomPanel.add(panel);
        controlPanel.setVisible(false);

        log.info("components set");
    }

    /**
     * Creates UI to interact with the game. Actions will not affect other players or the game
     * @return JPanel with buttons
     */
    protected JPanel createControlButtonPanel(){
        JPanel tableButtons = new JPanel(new GridLayout(1, 3));
        JPanel buttons = new JPanel(new GridLayout(3, 1));
        JButton button_lastStich = new JButton("letzter Stich");
        button_lastStich.addActionListener(e -> handler.queueOutMessage(new CurrentStich(new HashMap<>(), players.indexOf(c.name), true)));
        JButton button_clearTable = new JButton("Tisch leeren");
        button_clearTable.addActionListener(e -> clearPlayArea());
        buttons.add(button_lastStich);
        buttons.add(button_clearTable);
        if (isAdmin) {
            JButton button_adminPanel = new JButton("Admin");
            buttons.add(button_adminPanel);
            button_adminPanel.addActionListener(e -> {
                if(adminFrame !=null){
                    adminFrame.dispose();
                }
                createAdminUI();
            });
        }
        tableButtons.add(buttons);
        tableButtons.add(new JLabel());
        tableButtons.add(new JLabel());
        return tableButtons;
    }

    /**
     * Creates UI where game messages can be displayed
     * @return JPanel with labels
     */
    protected JPanel createMessageLabelPanel(){
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(serverMessageLabel);
        panel.add(gameMessageLabel);
        return panel;
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
        int distFromCenter = cardSize*c.distanceFromCenter/100;
        int theta = c.angleVariation - random.nextInt(c.angleVariation*2 + 1);
        int distVar = distFromCenter +  c.distanceVariation - random.nextInt(c.distanceVariation*2 + 1);
        BufferedImage img = cardImages.get(card.farbe+card.value);
        int halfHeight = canvasHeight/2;
        int halfWidth = canvasWidth/2;
        int anchorY = (int) (halfHeight*heightCorrection);
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
        at.translate(anchorX-(cardSize*RATIO/2),anchorY-(double)cardSize/2);
        Graphics2D g = (Graphics2D) playArea;
        g.drawImage(img, at, null);
    }

    /**
     * Create a new frame with extended functions to manipulate game in an atypical way.
     * Should only be necessary when something went wrong.
     */
    protected void createAdminUI() {

        adminFrame = new JFrame("Admin Panel");
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
        adminFrame.add(adminMainPanel);
        adminFrame.pack();
        adminFrame.setVisible(true);

        button_abortGame.addActionListener(e -> handler.queueOutMessage(new AbortGame()));

        JButton button_selectGame = new JButton("Spiel auswählen");

        adminMainPanel.add(button_selectGame);

        JButton button_close = new JButton("close");
        button_close.addActionListener(e -> adminFrame.dispose());
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
        cardWidth4Hand = panel.getWidth() / maxHandCards;
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

    protected JPanel createCardLabel(BaseCard card){
        JPanel p = new JPanel();
        JLabel label = new JLabel();
        label.setIcon(cardIcons.get(card.farbe+card.value));
        p.add(label);
        return p;
    }

    protected void getCardLabel4Hand(BaseCard card){
        JPanel p = new JPanel();
        JLabel label = new JLabel();
        label.setIcon(cardIcons.get(card.farbe+card.value));
        labelMap.put(card,label);
        cardMap.put(label,card);
        label.addMouseListener(cardClickAdapter);
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
        serverMessageLabel.setText("");
        gameMessageLabel.setText("");
        bottomPanel.revalidate();
        bottomPanel.repaint();;
    }

    protected void handleWait4Player(RequestObject message) {
        if (message.getParams().get("player").getAsString().equals(c.name)) {
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
    //

}
