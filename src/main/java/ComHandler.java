import base.AutoResetEvent;
import base.messages.AddPlayer;
import base.messages.RequestObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComHandler {

    private final AutoResetEvent ev = new AutoResetEvent(true);
    private final ConcurrentLinkedDeque<RequestObject> outMessages = new ConcurrentLinkedDeque<>();

    private Socket socket;
    private final String hostname;
    private final int port;
    private String name;
    private IInputputHandler client;

    public final AtomicBoolean wait = new AtomicBoolean(false);

    public ComHandler(String hostname, int port,IInputputHandler client){
        this.hostname = hostname;
        this.port = port;
        this.client = client;
        outMessageHandling();
    }


    public void outMessageHandling(){
        new Thread(() -> {
            while (true){
                try {
                    ev.waitOne(DokoServer.TIMEOUT);
                    if((socket==null || socket.isClosed())&&!wait.get()){
                        openTCPConnection(hostname,port);
                    }
                    if(socket!=null && socket.isConnected()) {
                        while (outMessages.peek() != null) {
                            if (SendByTCP(outMessages.peek())) {
                                outMessages.poll();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    //log.error(e.toString());
                }
            }
        }).start();
    }

    public void queueOutMessage(RequestObject message) {
        //log.info("queue: " + message.getCommand());
        if (message.getCommand().equals(AddPlayer.COMMAND)) {
            outMessages.forEach(requestObject -> {
                if (requestObject.getCommand().equals(AddPlayer.COMMAND)) {
                    outMessages.remove(requestObject);
                }
            });
            outMessages.addFirst(message);
        } else {
            outMessages.offer(message);
        }
        ev.set();

    }

    private boolean SendByTCP(RequestObject requestObject) {
        boolean sent = false;
        if (socket!=null && !socket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                String s = requestObject.toJson();
                //log.info("Sending to server: " + requestObject.getCommand());
                out.println(s);
                sent = true;
            } catch (IOException ex) {
                //log.error(ex.toString());
                //ev.set();
            }
        }
        else{
            //log.warn("socket was unexpectedly closed - Trying to reopen connection");
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
                    //log.error(ex.toString());
                }
                if (in != null) {
                    while (socket != null) {
                        try {
                            if ((ServerReply = in.readLine()) != null) {
                                if (ServerReply.length() > 0) {
                                    client.handleInput(RequestObject.fromString(ServerReply));
                                }
                            }
                        } catch (Exception ex) {
                            //log.error(ex.toString());
                            socket = null;
                        }
                    }
                }
            }
        }).start();
    }

    public void openTCPConnection(String hostname, int port) {
        queueOutMessage(new AddPlayer(name));
        wait.set(true);
        new Thread(() -> {
            while (socket == null) {
                try {
                    socket = new Socket(hostname, port);
                    Listen();
                    //log.info("Connected to Server");
                } catch (IOException e) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    //log.warn("Could not connect to Server: " + e);
                }
            }
            wait.set(false);
        }).start();

    }


    public void setClient(DokoClient handler) {
        this.client = handler;
    }

    public void setName(String name){
        this.name = name;
    }

    public void clearQueue() {
        outMessages.clear();
    }

    public boolean socketNotNull() {
        return socket!=null;
    }
}
