import base.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DokoServer {

    public final static String VERSION = "2.1.0";
    private final Logger log = new Logger(this.getClass().getName());
    Random random = new Random(System.currentTimeMillis());

    private final AutoResetEvent ev = new AutoResetEvent(true);

    private int currentStichNumber =0;
    private boolean wait4Gesund = false;
    private boolean wait4NextRound = false;
    private boolean schwein = false;
    private Stich stich;
    private List<Stich> stichList;
    private List<Card> armutCards;
    private HashMap<Integer,Boolean> readyMap;
    private HashMap<Integer,Integer> points;
    private HashMap<Integer,String> gameSelection = new HashMap<>();

    private int beginner =0;
    private int currentPlayer = 0;
    private String selectedGame = GameSelected.NORMAL;

    private int aufspieler = -1;
    private int armutplayer = -1;
    private int hochzeitSpieler = -1;
    private int spectator=4;

    public boolean listening = false;

    public final List<Player> players = new ArrayList<>();
    private List<Player> players2Ask = new ArrayList<>();
    private boolean wait4Partner;

    ConcurrentLinkedDeque<MessageIn> inMessages = new ConcurrentLinkedDeque<>();
    ConcurrentLinkedDeque<MessageOut> outMessages = new ConcurrentLinkedDeque<>();


    public DokoServer(int port){
        createCardList();
        inMessageHandling();
        outMessageHandling();
        startTCPServer(port);
    }


    public static List<Card> createCardList(){
        List<Card> cardList = new ArrayList<>();

        cardList.add(new Card(Statics.ZEHN, Statics.KREUZ,false));
        cardList.add(new Card(Statics.ZEHN, Statics.KREUZ,false));
        cardList.add(new Card(Statics.BUBE, Statics.KREUZ,true));
        cardList.add(new Card(Statics.BUBE, Statics.KREUZ,true));
        cardList.add(new Card(Statics.DAME, Statics.KREUZ,true));
        cardList.add(new Card(Statics.DAME, Statics.KREUZ,true));
        cardList.add(new Card(Statics.KOENIG, Statics.KREUZ,false));
        cardList.add(new Card(Statics.KOENIG, Statics.KREUZ,false));
        cardList.add(new Card(Statics.ASS, Statics.KREUZ,false));
        cardList.add(new Card(Statics.ASS, Statics.KREUZ,false));

        cardList.add(new Card(Statics.ZEHN, Statics.PIK,false));
        cardList.add(new Card(Statics.ZEHN, Statics.PIK,false));
        cardList.add(new Card(Statics.BUBE, Statics.PIK,true));
        cardList.add(new Card(Statics.BUBE, Statics.PIK,true));
        cardList.add(new Card(Statics.DAME, Statics.PIK,true));
        cardList.add(new Card(Statics.DAME, Statics.PIK,true));
        cardList.add(new Card(Statics.KOENIG, Statics.PIK,false));
        cardList.add(new Card(Statics.KOENIG, Statics.PIK,false));
        cardList.add(new Card(Statics.ASS, Statics.PIK,false));
        cardList.add(new Card(Statics.ASS, Statics.PIK,false));

        cardList.add(new Card(Statics.ZEHN, Statics.HERZ, true));
        cardList.add(new Card(Statics.ZEHN, Statics.HERZ,true));
        cardList.add(new Card(Statics.BUBE, Statics.HERZ,true));
        cardList.add(new Card(Statics.BUBE, Statics.HERZ,true));
        cardList.add(new Card(Statics.DAME, Statics.HERZ,true));
        cardList.add(new Card(Statics.DAME, Statics.HERZ,true));
        cardList.add(new Card(Statics.KOENIG, Statics.HERZ,false));
        cardList.add(new Card(Statics.KOENIG, Statics.HERZ,false));
        cardList.add(new Card(Statics.ASS, Statics.HERZ,false));
        cardList.add(new Card(Statics.ASS, Statics.HERZ,false));

        cardList.add(new Card(Statics.ZEHN, Statics.KARO, true));
        cardList.add(new Card(Statics.ZEHN, Statics.KARO,true));
        cardList.add(new Card(Statics.BUBE, Statics.KARO,true));
        cardList.add(new Card(Statics.BUBE, Statics.KARO,true));
        cardList.add(new Card(Statics.DAME, Statics.KARO,true));
        cardList.add(new Card(Statics.DAME, Statics.KARO,true));
        cardList.add(new Card(Statics.KOENIG, Statics.KARO,true));
        cardList.add(new Card(Statics.KOENIG, Statics.KARO,true));
        cardList.add(new Card(Statics.ASS, Statics.KARO,true));
        cardList.add(new Card(Statics.ASS, Statics.KARO,true));

        return cardList;
    }



    private void startTCPServer(int port) {
        new Thread(() -> {
            ServerSocket socket = null;
            log.info("Creating ServerSocket...");
            try {
                socket = new ServerSocket(port);
                log.info("ServerSocket created: " +socket.getInetAddress() +":"+socket.getLocalPort());
            } catch (IOException e) {
                log.info("Socket creation failed:\n"+ e);
            }
            while (true) {
                Socket connectionSocket;
                try {
                    assert socket != null;
                    listening = true;
                    connectionSocket = socket.accept();
                    log.info("Connection established");
                    if (connectionSocket != null) {
                        new Thread(() -> {
                            while (!connectionSocket.isClosed()) {
                                BufferedReader br;
                                try {
                                    br = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                                    String in = br.readLine();
                                    if (in != null) {
                                        inMessages.offer(new MessageIn(connectionSocket,in));
                                        ev.set();
                                    } else {
                                        log.info("socket => null");
                                        connectionSocket.close();
                                    }
                                } catch (IOException e) {
                                    if (e instanceof SocketException) {
                                        log.info("socket => null");
                                        try{
                                            connectionSocket.close();
                                        }catch (IOException ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                    log.error(e.toString());
                                }
                            }
                        }).start();
                    }
                } catch (IOException e) {
                    log.error(e.toString());
                }
            }
        }).start();
    }

    private void runGame(int player){
        points = new HashMap<>();
        for (int i = 0;i< players.size();i++) {
            points.put(i,0);
        }
        currentPlayer = player;
        aufspieler = -1;
        currentStichNumber =0;
        send2All(new GameType(selectedGame));
        send2All(new Wait4Player(players.stream().filter(p -> p.getNumber()==player).findAny().get().getName()));
    }

    private void inMessageHandling(){
        new Thread(() -> {
            while(true){
                try {
                    ev.waitOne();
                    while(inMessages.peek()!=null) {
                        log.info("messages to handle: " + inMessages.size());
                        handleInput(Objects.requireNonNull(inMessages.peek()));
                        inMessages.poll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleInput(MessageIn message) {
        RequestObject requestObject;
        Socket socketConnection = message.getSocket();
        String in = message.getInput();
        requestObject = RequestObject.fromString(in);
        players.stream().filter(player -> player.getSocket()==socketConnection).findFirst().ifPresent(
                player -> log.info("Received: " + requestObject.getCommand() + " from " + player.getName()));
        switch (requestObject.getCommand()) {
            case PutCard.COMMAND: {
                if (stich == null || stich.getCardMap().size() > 3) {
                    stich = new Stich();
                    currentStichNumber += 1;
                }
                stich.addCard(players.get(currentPlayer),new Card(
                        requestObject.getParams().get("wert").getAsString(),
                        requestObject.getParams().get("farbe").getAsString()));
                send2All(requestObject);
                //send2All(new CurrentStich(stich.getCardMap()));
                currentPlayer++;
                if(currentPlayer==spectator){
                    currentPlayer++;
                }
                if (currentPlayer >= players.size()) {
                    currentPlayer = 0;
                }
                if(currentPlayer==spectator){
                    currentPlayer++;
                }

                if (stich.getCardMap().size() > 3) {
                    try {
                        int winner = stich.getWinner(selectedGame, schwein);

                        stichList.add(stich);
                        try{
                            points.put(winner, points.get(winner) + stich.calculatePoints());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        currentPlayer = winner;
                        if(wait4Partner) {
                            if(currentStichNumber<4) {
                                if (hochzeitSpieler != winner) {
                                    players.get(winner).setRe(true, "Ehepartner");
                                    wait4Partner = false;
                                }
                            }
                            else {
                                wait4Partner = false;
                                log.info("kein Ehepartner gefunden");
                            }
                        }
                        send2All(new UpdateUserPanel(players.stream().filter(p->p.getNumber()==winner)
                                .findFirst().get().getName(),
                                createUserPanelString(players.stream().filter(p->p.getNumber()==winner).findFirst().get(),
                                        true)
                        ));
                        if (currentStichNumber > 9) {
                            EndDialog e = new EndDialog(players,stichList);
                            send2All(new GameEnd(e.getReString1(),e.getReString2(),e.getKontraString1(),e.getKontraString2()));
                            wait4NextRound= true;
                            readyMap = new HashMap<>();
                            for (int i=0;i<players.size();i++){
                                readyMap.put(i,false);
                            }
                            break;
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                if (currentStichNumber < 11) {
                    send2All(new Wait4Player(players.stream().filter(p->p.getNumber()==currentPlayer).findFirst().get().getName()));
                }
                break;
            }
            case AddPlayer.COMMAND:{
                String name = requestObject.getParams().get("player").getAsString();
                if(players.stream().noneMatch(player -> player.getName().equals(name))) {
                    players.add(new Player(requestObject.getParams().get("player").getAsString(),
                            players.size(), socketConnection, false));

                    List<String> list = new ArrayList<>();
                    players.forEach(p -> list.add(p.getName()));
                    send2All(new PlayersInLobby(list));
                    if (players.size() > 4) {
                        players.get(spectator).setSpectator(true);
                    }
                }
                else{
                    players.stream().filter(player -> player.getName().equals(name)).findFirst().ifPresent(player -> {
                        player.setSocket(socketConnection);
                        List<String> list = new ArrayList<>();
                        players.forEach(p -> list.add(p.getName()));
                        queueOut(player, new PlayersInLobby(list));
                    });
                }
                break;
            }
            case StartGame.COMMAND:{
                send2All(new StartGame());
                send2All(new AnnounceSpectator(spectator));
                shuffleCards();
                break;
            }
            case ReadyForNextRound.COMMAND:{
                if(wait4NextRound){
                    readyMap.put(requestObject.getParams().get("player").getAsInt(),true);
                }
                if(readyMap.values().stream().allMatch(p-> p)){
                    nextGame();
                }
                break;
            }
            case GameSelected.COMMAND:{
                if(wait4Gesund) {
                    gameSelection.put(
                            requestObject.getParams().get("player").getAsInt(),
                            requestObject.getParams().get("game").getAsString());
                }
                if(gameSelection.keySet().size()>3){
                    setGameToPlay(gameSelection);
                }
                else {
                    StringBuilder s = new StringBuilder("Warte auf " + (4 -gameSelection.size())+ " Spieler");
                    players.forEach(player -> {
                        if (gameSelection.containsKey(player.getNumber())) {
                            queueOut(player, new DisplayMessage(s.toString()));
                        }
                    });
                }
                break;
            }
            case SendCards.COMMAND:{
                armutCards = new ArrayList<>();
                if(requestObject.getParams().get("receiver").getAsString().equals(SendCards.RICH)) {
                    requestObject.getParams().get("cards").getAsJsonArray().
                            forEach(p -> armutCards.add(
                                    new Card(p.getAsString().split(" ")[1],
                                            p.getAsString().split(" ")[0])));
                    askNextPlayer2GetArmut();
                }
                else if(requestObject.getParams().get("receiver").getAsString().equals(SendCards.POOR)){
                    send2All(new DisplayMessage(Strings.getString(Strings.ARMUT_RETURN,
                            players.get(armutplayer).getName(),
                            getTrumpfCardCount(requestObject.getParams()))));
                    /*
                    send2All(new DisplayMessage(
                            players.get(armutplayer).getName()
                                    +" bekommt "
                                    + getTrumpfCardCount(requestObject.getParams())
                                    + " Trumpf zurück"));

                     */
                    queueOut(players.get(armutplayer),
                            requestObject);
                    players.get(armutplayer).setRe(true, "ist reich");
                    players2Ask = new ArrayList<>();
                    runGame(beginner);
                }
                break;
            }
            case GetArmut.COMMAND:{
                if(requestObject.getParams().get("getArmut").getAsBoolean()){
                    queueOut(players.get(requestObject.getParams().get("player").getAsInt()),
                            new SendCards(armutCards,SendCards.RICH));
                    players.get(requestObject.getParams().get("player").getAsInt()).setRe(true, "ist arm");
                }else{
                    send2All(new DisplayMessage(
                            players.get(requestObject.getParams().get("player").getAsInt()).getName()
                                    + " lehnt die Armut ab"));
                    askNextPlayer2GetArmut();
                }
                break;
            }
            case SchweinExists.COMMAND:{
                schwein = true;
                break;
            }
            case CurrentStich.LAST:{
                queueOut(players.get(requestObject.getParams().get("player").getAsInt()),
                        new CurrentStich(stichList.get(currentStichNumber-2).getCardMap(),CurrentStich.LAST));
                break;
            }
            case AbortGame.COMMAND:{
                send2All(new GameEnd(points,stichList,players));
                wait4NextRound= true;
                readyMap = new HashMap<>();
                for (int i=0;i<players.size();i++){
                    readyMap.put(i,false);
                }
                break;
            }
            case ShowStich.COMMAND:{
                try {
                    send2All(new CurrentStich(stichList.get(requestObject.getParams()
                            .get("stichNumber").getAsInt()).getCardMap(),CurrentStich.SPECIFIC));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                break;
            }
            case GetVersion.COMMAND:{
                queueOut(socketConnection,
                        new GetVersion("Server",VERSION),true);
                break;
            }
            default:{

            }
        }
    }

    private String createUserPanelString(Player player, boolean hatStich) {
        StringBuilder s = new StringBuilder();
        s.append("<html>").append(player.getName());
        if(hatStich){
            s.append("<br>hat Stich(e)");
        }
        s.append("</hmtl>");
        return s.toString();
    }

    private int getTrumpfCardCount(JsonObject object) {
        JsonArray array = object.get("cards").getAsJsonArray();
        List<Card> cardList = new ArrayList<>();
        for(JsonElement element: array){
            String cardString = element.getAsString();
            Card card = new Card(cardString.split(" ")[1], cardString.split(" ")[0]);
            cardList.add(card);
        }
        return ((int)cardList.stream().filter(card -> card.trumpf).count());
    }

    private void nextGame() {
        if(selectedGame.equals(GameSelected.NORMAL)
                ||selectedGame.equals(GameSelected.ARMUT)){
            beginner++;
            if(beginner>players.size()-1){
                beginner=0;
            }
            if(players.size()>4) {
                spectator++;
                if (spectator > players.size() - 1) {
                    spectator = 0;
                }
            }
            players.forEach(player ->{
                player.setSpectator(false);
                player.setRe(false);
            });
            wait4Partner = false;
            hochzeitSpieler = -1;
            if(players.size()>4) {
                players.get(spectator).setSpectator(true);
                send2All(new AnnounceSpectator(spectator));
            }
        }
        shuffleCards();
    }

    public void send2All(RequestObject message) {
        for(Player player: players){
            queueOut(player.getSocket(),message,false);
        }
        ev.set();
    }

    private void createListOfPotentialPartners(){
        players2Ask = new ArrayList<>();
        log.info("Armutplayer= " + armutplayer);
        int korrValue = 1;
        for(int i=0;i<players.size()-1;i++){
            log.info("korrValue= " + korrValue);
            log.info("i= " + i);
            int p = i+armutplayer+korrValue;
            log.info("p= " + p);
            if (p>players.size()-1){
                korrValue -= players.size();
                p = i+armutplayer+korrValue;
            }
            if(!players.get(p).isSpectator()){
                players2Ask.add(players.get(p));
            }
        }
    }

    private void askNextPlayer2GetArmut(){
        if(players2Ask.size()>0) {
            log.info("Ask " + players2Ask.get(0));
            queueOut(players2Ask.get(0), new GetArmut());
            players2Ask.remove(0);
        }
        else{
            send2All(new DisplayMessage("Armut wurde nicht mitgenommen, es wird neu ausgeteilt"));
            shuffleCards();
        }

    }

    private void setGameToPlay(HashMap<Integer,String> selection) {
        players.forEach(player -> player.setRe(false));
        if(selection.values().stream().allMatch(p->p.equals(GameSelected.NORMAL))){
            selectedGame = GameSelected.NORMAL;
            aufspieler=-1;
            players.forEach(player -> {
                if(player.hasCard(Statics.KREUZ,Statics.DAME)){
                    player.setRe(true, "hat Kreuz Dame");
                }
            });
            send2All(new DisplayMessage(Strings.getString(Strings.NORMALES_SPIEL)));
            runGame(beginner);
        }
        else if(selection.containsValue(GameSelected.KOENIGE)){
            int index =-1;
            for (int i: selection.keySet()){
                if(selection.get(i).equals(GameSelected.KOENIGE)){
                    index= i;
                }
            }
            int finalIndex = index;
            send2All(new DisplayMessage("Es wird neu gegeben. "+ players.get(finalIndex).getName()+ " hat zuviele Koenige."));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            shuffleCards();
        }
        else if(selection.containsValue(GameSelected.DAMEN)
                || selection.containsValue(GameSelected.BUBEN)
                || selection.containsValue(GameSelected.BUBENDAMEN)
                || selection.containsValue(GameSelected.FLEISCHLOS)
                || selection.containsValue(GameSelected.KREUZ)
                || selection.containsValue(GameSelected.PIK)
                || selection.containsValue(GameSelected.HERZ)
                || selection.containsValue(GameSelected.KARO)){
            int checkPlayer = beginner;
            while(true){
                if(selection.containsKey(checkPlayer)
                        && (selection.get(checkPlayer).equals(GameSelected.DAMEN)
                        ||selection.get(checkPlayer).equals(GameSelected.BUBEN)
                        ||selection.get(checkPlayer).equals(GameSelected.BUBENDAMEN)
                        ||selection.get(checkPlayer).equals(GameSelected.FLEISCHLOS)
                        ||selection.get(checkPlayer).equals(GameSelected.KREUZ)
                        ||selection.get(checkPlayer).equals(GameSelected.PIK)
                        ||selection.get(checkPlayer).equals(GameSelected.HERZ)
                        ||selection.get(checkPlayer).equals(GameSelected.KARO))){
                    selectedGame = selection.get(checkPlayer);
                    aufspieler = checkPlayer;
                    send2All(new DisplayMessage(players.get(aufspieler).getName()+ " spielt "+selectedGame+"."));
                    players.get(aufspieler).setRe(true, "spielt Solo");
                    runGame(aufspieler);
                    return;
                }
                else{
                    checkPlayer++;
                    if (checkPlayer>players.size()){
                        checkPlayer=0;
                    }
                }
            }
        }
        else if (selection.containsValue(GameSelected.ARMUT)){
            int index =-1;
            for (int i: selection.keySet()){
                if(selection.get(i).equals(GameSelected.ARMUT)){
                    index= i;
                    selectedGame = GameSelected.ARMUT;
                }
            }

            int finalIndex = index;
            armutplayer = finalIndex;
            createListOfPotentialPartners();
            send2All(new DisplayMessage(players.get(finalIndex).getName()+ " hat nur "+
                    players.get(finalIndex).getHand().stream().filter(card -> card.trumpf).count() +" Trumpf."));
            queueOut(players.stream().filter(p -> p.getNumber()==finalIndex).findAny().get(),
                    new SelectCards4Armut());
        }
        else if(selection.containsValue(GameSelected.HOCHZEIT)){
            selectedGame = GameSelected.NORMAL;
            aufspieler=-1;
            for(Integer i :selection.keySet()) {
                if(selection.get(i).equals(GameSelected.HOCHZEIT)){
                    players.get(i).setRe(true, "möchte heiraten");
                    send2All(new DisplayMessage(players.get(i).getName() + " möchte heiraten"));
                    hochzeitSpieler = i;
                    break;
                }
            }
            wait4Partner = true;
            runGame(beginner);
        }
        wait4Gesund = false;
    }

    public boolean sendReply(MessageOut message) {
        Socket socketConnection = message.getSocket();
        RequestObject requestObject = message.getOutput();
        boolean sent = false;
        if (!socketConnection.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socketConnection.getOutputStream())), true);
                String s = requestObject.toJson();
                players.stream().filter(player -> player.getSocket() == socketConnection).findFirst().ifPresent(
                        player -> log.info("Send to " + player.getName() + ": " + s)
                );
                out.println(s);
                sent = true;
            } catch (IOException ex) {
                log.error(ex.toString());
            }
        }
        return sent;
    }

    public void queueOut(Player player, RequestObject message) {
        queueOut(player.getSocket(),message,true);
    }

    public void queueOut(Socket socket, RequestObject message, boolean resetEvent){
        outMessages.offer(new MessageOut(socket,message));
        log.error("added message: " + message.getCommand());
        if(resetEvent) {
            ev.set();
        }
    }


    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    ev.waitOne(5000);
                    while(outMessages.peek()!=null){
                        if(sendReply(outMessages.peek())) {
                            outMessages.poll();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void shuffleCards() {
        for (Player player1 : players) {
            send2All(new UpdateUserPanel(player1.getName(),createUserPanelString(player1, false)));
        }
        stichList = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        List<Card> cardList = createCardList();

        players.forEach(player -> {
            player.setHand(new ArrayList<>());
            if(!player.isSpectator()) {
                for (int i = 0; i < 10; i++) {
                    Card card = cardList.get(random.nextInt(cardList.size()));
                    player.getHand().add(card);
                    cardList.remove(card);
                }
            }
        });

        for (Player player : players) {
            if (!player.isSpectator()) {
                Cards cards = new Cards(player.getHand());
                queueOut(player, cards);
            }
        }
        wait4Gesund = true;
        armutplayer =-1;
        schwein = false;
        gameSelection = new HashMap<>();
        send2All(new SelectGame());
    }


    @Override
    public String toString() {
        return "<html>" + selectedGame + "<br>" +
                spectator + "<br>" +
                currentPlayer + "<br>";
    }


    public void startGame() {
        send2All(new StartGame());
        /*
        if(spectator>players.size()) {
            log.info(players.get(spectator) + " is watching the game");
        }
        */
        send2All(new AnnounceSpectator(spectator));
        shuffleCards();
    }
}
