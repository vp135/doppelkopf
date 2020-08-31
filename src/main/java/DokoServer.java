import base.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DokoServer {

    private final Logger log = new Logger(this.getClass().getName());

    private static String KREUZ = "Kreuz";
    private static String PIK = "Pik";
    private static String HERZ = "Herz";
    private static String KARO = "Karo";

    private static String ZEHN = "10";
    private static String BUBE = "Bube";
    private static String DAME = "Dame";
    private static String KOENIG = "Koenig";
    private static String ASS = "Ass";



    private Stich stich;
    private List<Stich> stichList;
    Random random = new Random(System.currentTimeMillis());

    private boolean lobby = true;
    private int currentPlayer = 0;
    private int currentStichNumber =0;
    private int beginner =0;
    private boolean schwein = false;
    private HashMap<Integer,Integer> points;
    private boolean wait4NextRound = false;
    private HashMap<Integer,Boolean> readyMap;
    private boolean wait4Gesund = false;
    private String selectedGame = GameSelected.NORMAL;

    private HashMap<Integer,String> gameSelection = new HashMap<>();
    private int aufspieler = -1;
    private int armutplayer = -1;
    private List<Card> armutCards;
    private int player2Ask =-1;
    private int spectator=4;

    private List<Player> players = new ArrayList<>();


    public DokoServer(int port){
        createCardList();
        new Thread(() -> startTCPServer(port)).start();
        //matchmaking();
    }

    private List<Card> createCardList(){
        List<Card> cardList = new ArrayList<>();
        cardList.add(new Card(ZEHN, KREUZ));
        cardList.add(new Card(ZEHN, KREUZ));
        cardList.add(new Card(BUBE, KREUZ));
        cardList.add(new Card(BUBE, KREUZ));
        cardList.add(new Card(DAME, KREUZ));
        cardList.add(new Card(DAME, KREUZ));
        cardList.add(new Card(KOENIG, KREUZ));
        cardList.add(new Card(KOENIG, KREUZ));
        cardList.add(new Card(ASS, KREUZ));
        cardList.add(new Card(ASS, KREUZ));

        cardList.add(new Card(ZEHN, PIK));
        cardList.add(new Card(ZEHN, PIK));
        cardList.add(new Card(BUBE, PIK));
        cardList.add(new Card(BUBE, PIK));
        cardList.add(new Card(DAME, PIK));
        cardList.add(new Card(DAME, PIK));
        cardList.add(new Card(KOENIG, PIK));
        cardList.add(new Card(KOENIG, PIK));
        cardList.add(new Card(ASS, PIK));
        cardList.add(new Card(ASS, PIK));

        cardList.add(new Card(ZEHN, HERZ));
        cardList.add(new Card(ZEHN, HERZ));
        cardList.add(new Card(BUBE, HERZ));
        cardList.add(new Card(BUBE, HERZ));
        cardList.add(new Card(DAME, HERZ));
        cardList.add(new Card(DAME, HERZ));
        cardList.add(new Card(KOENIG, HERZ));
        cardList.add(new Card(KOENIG, HERZ));
        cardList.add(new Card(ASS, HERZ));
        cardList.add(new Card(ASS, HERZ));

        cardList.add(new Card(ZEHN, KARO));
        cardList.add(new Card(ZEHN, KARO));
        cardList.add(new Card(BUBE, KARO));
        cardList.add(new Card(BUBE, KARO));
        cardList.add(new Card(DAME, KARO));
        cardList.add(new Card(DAME, KARO));
        cardList.add(new Card(KOENIG, KARO));
        cardList.add(new Card(KOENIG, KARO));
        cardList.add(new Card(ASS, KARO));
        cardList.add(new Card(ASS, KARO));

        return cardList;
    }



    private void startTCPServer(int port) {
        ServerSocket socket = null;

        log.info("Creating ServerSocket...");
        try {
            socket = new ServerSocket(port);
            log.info("ServerSocket created");
        } catch (IOException e) {
            log.info("Socket creation failed:\n"+ e);
        }
        while (true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = socket.accept();
                log.info("Connection established");
                if (connectionSocket != null) {
                    Socket finalConnectionSocket1 = connectionSocket;
                    new Thread(() -> {
                        boolean running = true;
                        while (running) {
                            BufferedReader br;
                            try {
                                br = new BufferedReader(new InputStreamReader(finalConnectionSocket1.getInputStream()));
                                String in = br.readLine();
                                if (in != null) {
                                    handleInput(finalConnectionSocket1, in);
                                    //log.info("Received: " + in);

                                } else {
                                    log.info("socket => null");
                                    running = false;
                                    finalConnectionSocket1.close();
                                }
                            } catch (IOException e) {
                                if (e instanceof SocketException) {
                                    log.info("socket => null");
                                    running = false;
                                }
                                log.error(e.toString());
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
            //Receiving
        }
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

    private void handleInput(Socket socketConnection, String in) {
        RequestObject requestObject;
        try {
            requestObject = RequestObject.fromString(in);
            if(requestObject.getCommand()!=TcpHeartbeat.COMMAND){
                log.info("Received: " + requestObject.getCommand());
            }
            switch (requestObject.getCommand()) {
                case PutCard.COMMAND: {
                    if (stich == null || stich.getCardMap().size() > 3) {
                        stich = new Stich();
                        currentStichNumber += 1;
                    }
                    stich.getCardMap().put(currentPlayer, new Card(
                            requestObject.getParams().get("wert").getAsString(),
                            requestObject.getParams().get("farbe").getAsString()));
                    send2All(new CurrentStich(stich.getCardMap()));
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
                            int winner = stich.getWinner(selectedGame, currentPlayer, schwein);

                        stichList.add(stich);
                        try{
                            points.put(winner, points.get(winner) + stich.calculatePoints());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        log.info("Winner: " + winner + "(" + stich.getCardMap().get(winner) + ")");
                        currentPlayer = winner;
                        send2All(new UpdateUserPanel(players.stream().filter(p->p.getNumber()==winner).findFirst().get().getName(),"hat Stich(e)"));
                        if (currentStichNumber > 9) {
                            send2All(new GameEnd(points,stichList));
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
                case ShuffleCards.COMMAND: {
                    shuffleCards();
                    runGame(beginner);
                    break;
                }
                case AddPlayer.COMMAND:{
                    players.add(new Player(requestObject.getParams().get("player").getAsString(),
                            players.size(),socketConnection,false));

                    List<String> list = new ArrayList<>();
                    players.forEach(p->list.add(p.getName()));
                    send2All(new PlayersInLobby(list));
                    if(players.size()>4) {
                        players.get(spectator).setSpectator(true);
                    }

                    break;
                }
                case StartGame.COMMAND:{
                    send2All(new StartGame());
                    send2All(new AnnounceSpectator(spectator));
                    shuffleCards();
                    lobby=false;
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
                        sendReply(players.stream().filter(player ->
                                player.getNumber()==armutplayer).findFirst().get().getSocket(),
                                requestObject.toJson());
                        runGame(beginner);
                    }
                    break;
                }
                case GetArmut.COMMAND:{
                    if(requestObject.getParams().get("getArmut").getAsBoolean()){
                        sendReply(players.get(requestObject.getParams().get("player").getAsInt()).getSocket(),
                                new SendCards(armutCards,SendCards.RICH).toJson());
                    }else{
                        askNextPlayer2GetArmut();
                    }
                    break;
                }
                case SchweinExists.COMMAND:{
                    schwein = true;
                    break;
                }
                case CurrentStich.LAST:{
                    sendReply(players.get(requestObject.getParams().get("player").getAsInt()).getSocket(),
                            new CurrentStich(stichList.get(currentStichNumber-2).getCardMap(),
                                    requestObject.getParams().get("player").getAsInt()).toJson());
                    break;
                }
                case TcpHeartbeat.COMMAND:{

                    break;
                }
                case AbortGame.COMMAND:{
                    send2All(new GameEnd(points,stichList));
                    wait4NextRound= true;
                    readyMap = new HashMap<>();
                    for (int i=0;i<players.size();i++){
                        readyMap.put(i,false);
                    }
                    break;
                }
                case ShowStich.COMMAND:{
                    try {
                        send2All(CurrentStich.specificStich(stichList.get(requestObject.getParams()
                                .get("stichNumber").getAsInt()).getCardMap()));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    break;
                }
                default:{

                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
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
            players.forEach(player -> player.setSpectator(false));
            if(players.size()>4) {
                players.get(spectator).setSpectator(true);
                send2All(new AnnounceSpectator(spectator));
            }
        }
        shuffleCards();
    }

    private void send2All(RequestObject requestObject) {
        log.info("Send to All: "+ requestObject.getCommand());
        players.forEach(player ->{
            System.out.println(player.getName());
            sendReply(player.getSocket(),requestObject.toJson());
        });
    }

    private void askNextPlayer2GetArmut() {
        if(player2Ask<0) {
            player2Ask = armutplayer+1;
        }
        else{
            player2Ask++;
            if(player2Ask==spectator){
                player2Ask++;
            }
        }
        if(player2Ask>players.size()-1){
            player2Ask = 0;
        }

        if(player2Ask!=armutplayer){
            log.info("Ask "+ players.get(player2Ask).getName());
            sendReply(players.stream().filter(p -> p.getNumber()==player2Ask).findAny().get().getSocket(),new GetArmut().toJson());
        }
        else {
            send2All(new DisplayMessage("Armut wurde nicht mitgenommen, es wird neu ausgeteilt"));
            shuffleCards();
        }
    }

    private void setGameToPlay(HashMap<Integer,String> selection) {
        if(selection.values().stream().allMatch(p->p.equals(GameSelected.NORMAL))){
            selectedGame = GameSelected.NORMAL;
            aufspieler=-1;
            send2All(new DisplayMessage("normales Spiel"));
            runGame(beginner);
        }
        else if(selection.values().contains(GameSelected.KOENIGE)){
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
        else if(selection.values().contains(GameSelected.DAMEN)
                ||selection.values().contains(GameSelected.BUBEN)
                ||selection.values().contains(GameSelected.BUBENDAMEN)
                ||selection.values().contains(GameSelected.FLEISCHLOS)
                ||selection.values().contains(GameSelected.KREUZ)
                ||selection.values().contains(GameSelected.PIK)
                ||selection.values().contains(GameSelected.HERZ)
                ||selection.values().contains(GameSelected.KARO)){
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
        else if (selection.values().contains(GameSelected.ARMUT)){
            int index =-1;
            for (int i: selection.keySet()){
                if(selection.get(i).equals(GameSelected.ARMUT)){
                    index= i;
                    selectedGame = GameSelected.ARMUT;
                }
            }

            int finalIndex = index;
            armutplayer = finalIndex;
            send2All(new DisplayMessage(players.get(finalIndex).getName()+ " hat eine "+selectedGame+"."));
            sendReply(players.stream().filter(p -> p.getNumber()==finalIndex).findAny().get().getSocket(),
                    new SelectCards4Armut().toJson());
        }
    }

    public void sendReply(Socket connection, String msg) {
        new Thread(() -> {
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
                    out.println(msg + "\n");
                } catch (IOException ex) {
                    log.error(ex.toString());
                }
        }).start();
    }

    private void shuffleCards() {
        for (Player player1 : players) {
            send2All(new UpdateUserPanel(player1.getName(), ""));
        }
        stichList= new ArrayList<>();
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
                Socket socket = player.getSocket();
                String cards = new Cards(player.getHand()).toJson();
                sendReply(socket, cards);
            }
        }


        wait4Gesund = true;
        player2Ask =-1;
        armutplayer =-1;
        schwein = false;
        gameSelection = new HashMap<>();
        send2All(new SelectGame());

    }

}
