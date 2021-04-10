import base.*;
import base.doko.*;
import base.doko.messages.*;
import base.messages.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class DokoServer extends BaseServer {

    public final static String VERSION = "3.2.1";
    public static final long TIMEOUT = 1000;
    private final Logger log = new Logger(this.getClass().getName(),1);
    Random random = new Random(System.currentTimeMillis());

    private final AutoResetEvent ev = new AutoResetEvent(true);

    private int currentStichNumber =0;
    private boolean wait4Gesund = false;
    private boolean wait4NextRound = false;
    private boolean schwein = false;
    private Stich stich;
    private List<Stich> stichList;
    private List<BaseCard> armutCards;
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


    public DokoServer(int port, Configuration c){
        log.setLoglevel(c.logLevel);
        inMessageHandling();
        outMessageHandling();
        startTCPServer(port);
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
                                        log.info("incoming data was null "+ players.stream().filter(player -> player.getSocket().equals(connectionSocket)).findFirst().get().getName());
                                        connectionSocket.close();
                                    }
                                } catch (IOException e) {
                                    if (e instanceof SocketException) {
                                        log.info(e.toString()+ players.stream().filter(player -> player.getSocket().equals(connectionSocket)).findFirst().get().getName());
                                        try{
                                            connectionSocket.close();
                                        }catch (IOException ex){
                                            log.error(ex.toString());
                                        }
                                    }
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
                    ev.waitOne(TIMEOUT);
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
                    stich = new Stich(players, currentStichNumber, selectedGame);
                    currentStichNumber++;
                }
                stich.addCard(players.get(currentPlayer),new Card(
                        requestObject.getParams().get("wert").getAsString(),
                        requestObject.getParams().get("farbe").getAsString()));
                send2All(requestObject);
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
                        int winner = stich.getWinner(schwein);

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
                                .findFirst().get().getName()," hat Stich(e)"));
                        if (currentStichNumber > 9) {
                            EndIt();
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
            /*case StartGame.COMMAND:{
                send2All(new StartGame());
                send2All(new AnnounceSpectator(spectator,aufspieler));
                shuffleCards();
                break;
            }

             */
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
                    players.stream().filter(player -> player.getNumber()!=requestObject.getParams().get("player").getAsInt())
                            .collect(Collectors.toList()).forEach(player -> queueOut(player,new DisplayMessage(
                            players.get(requestObject.getParams().get("player").getAsInt()).getName() + " nimmt die Armut auf"
                    )));
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
                if(stichList.size()>0) {
                    try {
                        CurrentStich cs = new CurrentStich(stichList.get(stichList.size()-1).getCardMap(), CurrentStich.LAST);
                        queueOut(players.get(requestObject.getParams().get("player").getAsInt()), cs);
                    }
                    catch (Exception ex){
                        log.warn(ex.toString());
                    }
                }
                break;
            }
            case AbortGame.COMMAND:{
                EndIt();
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
                log.warn(String.format("no handler for message type: %s", requestObject.getCommand()));
            }
        }
    }

    private void EndIt() {
        EndDialog e = new EndDialog(players,stichList);
        send2All(new GameEnd(e.getReString1(),e.getReString2(),e.getKontraString1(),e.getKontraString2(),e.getRemaining()));
        wait4NextRound= true;
        readyMap = new HashMap<>();
        for (int i=0;i<players.size();i++){
            readyMap.put(i,false);
        }
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
                spectator = beginner-1;
                if (spectator<0){
                    spectator = players.size()-1;
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
            }
            send2All(new AnnounceSpectator(spectator,aufspieler));
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
        wait4Gesund = false;
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
        log.info("added message: " + message.getCommand());
        if(resetEvent) {
            ev.set();
        }
    }


    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    ev.waitOne(TIMEOUT);
                    if(outMessages.peek()==null){
                        if(outMessages.peek()==null){
                            try {
                                outMessages.offer(new MessageOut(players.get(0).getSocket(),
                                        new DisplayMessage(LocalDateTime.now().toString())));
                            }catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
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
            send2All(new UpdateUserPanel(player1.getName(), ""));
        }
        stichList = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
        List<Card> cardList = Card.createCardList();

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
        send2All(new StartGame("DOKO"));
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        send2All(new AnnounceSpectator(spectator,aufspieler));
        shuffleCards();
    }




    public Stich getStich(int stichNumber) {
        return stichList.get(stichNumber);
    }
}
