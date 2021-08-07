import base.AutoResetEvent;
import base.IInputputHandler;
import base.Logger;
import base.messages.MessageAddPlayer;
import base.messages.Message;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComClient {

    private static final long TIMEOUT = 5000;
    private final AutoResetEvent ev = new AutoResetEvent(true);
    private final ConcurrentLinkedDeque<Message> outMessages = new ConcurrentLinkedDeque<>();

    private Socket socket;
    private final String hostname;
    private final int port;
    private String name;
    private IInputputHandler client;

    public final AtomicBoolean wait = new AtomicBoolean(false);

    private final Logger log = new Logger(name,4,true);

    public ComClient(String hostname, int port, IInputputHandler client, String name){
        this.hostname = hostname;
        this.port = port;
        this.client = client;
        this.name = name;
    }


    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    ev.waitOne(TIMEOUT);
                    if((socket==null || socket.isClosed())&&!wait.get()){
                        openTCPConnection();
                    }
                    if(socket!=null && socket.isConnected()) {
                        while (outMessages.peek() != null) {
                            if (SendByTCP(outMessages.peek())) {
                                outMessages.poll();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //log.error(e.toString());
                }
            }
        }).start();
    }

    public void queueOutMessage(Message message) {
        log.info("queue: " + message.getCommand());
        if (message.getCommand().equals(MessageAddPlayer.COMMAND)) {
            outMessages.forEach(requestObject -> {
                if (requestObject.getCommand().equals(MessageAddPlayer.COMMAND)) {
                    outMessages.remove(requestObject);
                }
            });
            outMessages.addFirst(message);
        } else {
            outMessages.offer(message);
        }
        ev.set();

    }

    private boolean SendByTCP(Message message) {
        boolean sent = false;
        if (socket!=null && !socket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                String s = message.toJson();
                log.info("Sending to server: " + message.getCommand());
                out.println(s);
                sent = true;
            } catch (IOException ex) {
                log.error(ex.toString());
                //ev.set();
            }
        }
        else{
            log.warn("socket was unexpectedly closed - Trying to reopen connection");
            socket = null;
        }
        return sent;
    }

    private void Listen() {
        new Thread(() -> {
            if (socket != null) {
                String ServerReply;
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    log.error(ex.toString());
                }
                if (in != null) {
                    while (socket != null) {
                        try {
                            if ((ServerReply = in.readLine()) != null) {
                                if (ServerReply.length() > 0) {
                                    //log.info(ServerReply);
                                    client.handleInput(Message.fromString(ServerReply));
                                }
                            }
                        } catch (Exception ex) {
                            log.error(ex.toString());
                            socket = null;
                        }
                    }
                }
            }
        }).start();
    }

    public void openTCPConnection() {
        queueOutMessage(new MessageAddPlayer(name));
        wait.set(true);
        new Thread(() -> {
            while (socket == null) {
                try {
                    socket = new Socket(hostname, port);
                    Listen();
                    log.info("Connected to Server");
                } catch (IOException e) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    log.warn("Could not connect to Server: " + e);
                }
            }
            wait.set(false);
            ev.set();
        }).start();

    }


    public void setClient(BaseClient handler) {
        this.client = handler;
    }

    public void clearQueue() {
        outMessages.clear();
    }

    public boolean socketNotNull() {
        return socket!=null;
    }

    public void start() {
        openTCPConnection();
        outMessageHandling();
    }
}
