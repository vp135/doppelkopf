import base.AutoResetEvent;
import base.Logger;
import base.MessageIn;
import base.MessageOut;
import base.messages.RequestObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ComServer {

    protected Logger log = new Logger(this.getClass().getName(),4);


    private final ConcurrentLinkedDeque<MessageIn> inMessages = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<MessageOut> outMessages = new ConcurrentLinkedDeque<>();
    private final AutoResetEvent evOut = new AutoResetEvent(true);
    private final AutoResetEvent evIn = new AutoResetEvent(true);
    public static final long TIMEOUT = 1000;

    private IServerMessageHandler server;

    public boolean listening = false;


    public ComServer(int port){
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
                                        evIn.set();
                                    } else {
                                        log.info("test1");
                                        //log.info("incoming data was null "+ players.stream().filter(player -> player.getSocket().equals(connectionSocket)).findFirst().get().getName());
                                        connectionSocket.close();
                                    }
                                } catch (IOException e) {
                                    if (e instanceof SocketException) {
                                        log.info("test2");
                                        //log.info(e.toString()+ players.stream().filter(player -> player.getSocket().equals(connectionSocket)).findFirst().get().getName());
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

    private void inMessageHandling(){
        new Thread(() -> {
            while(true){
                try {
                    evIn.waitOne(TIMEOUT);
                    while(inMessages.peek()!=null) {
                        log.info("messages to handle: " + inMessages.size());
                        server.handleInput(Objects.requireNonNull(inMessages.peek()));
                        inMessages.poll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    evOut.waitOne(TIMEOUT);
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

    public boolean sendReply(MessageOut message) {
        Socket socketConnection = message.getSocket();
        RequestObject requestObject = message.getOutput();
        boolean sent = false;
        if (!socketConnection.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socketConnection.getOutputStream())), true);
                String s = requestObject.toJson();
                out.println(s);
                log.info(s);
                sent = true;
            } catch (IOException ex) {
                log.error(ex.toString());
            }
        }
        return sent;
    }

    public void queueOut(Socket socket, RequestObject message, boolean resetEvent){
        outMessages.offer(new MessageOut(socket,message));
        log.info("added message: " + message.getCommand());
        if(resetEvent) {
            evOut.set();
        }
    }

    public void setServer(BaseServer server) {
        this.server = server;
    }

    public void send2All(List<Socket> sockets, RequestObject message) {
        sockets.forEach(socket -> queueOut(socket,message,false));
        evOut.set();
    }
}
