import base.AutoResetEvent;
import base.IInputputHandler;
import base.Logger;
import base.messages.AddPlayer;
import base.messages.RequestObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComClient {

    private static final long INITIAL_RECONNECT_DELAY = 500;
    private static final long MAX_RECONNECT_DELAY = 15000;
    private static final long HEARTBEAT_INTERVAL = 15000;
    private static final long HEARTBEAT_TIMEOUT = 45000;
    private static final int CONNECT_TIMEOUT = 3000;
    private final AutoResetEvent ev = new AutoResetEvent(true);
    private final ConcurrentLinkedDeque<RequestObject> outMessages = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean connectionLoopStarted = new AtomicBoolean(false);
    private final AtomicBoolean outputLoopStarted = new AtomicBoolean(false);

    private volatile Socket socket;
    private volatile BufferedWriter writer;
    private volatile long lastHeartbeatResponseAt;
    private final String hostname;
    private final int port;
    private final String name;
    private volatile IInputputHandler client;

    public final AtomicBoolean wait = new AtomicBoolean(false);

    private final Logger log;

    public ComClient(String hostname, int port, IInputputHandler client, String name){
        this.hostname = hostname;
        this.port = port;
        this.client = client;
        this.name = name;
        this.log = new Logger(name,4,true);
    }

    public void outMessageHandling(){
        if (!outputLoopStarted.compareAndSet(false, true)) {
            return;
        }
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    ev.waitOne(1000);
                    while (isSocketActive()) {
                        RequestObject message = outMessages.poll();
                        if (message == null) {
                            break;
                        }
                        if (!sendByTCP(message)) {
                            outMessages.addFirst(message);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "client-message-writer");
        thread.setDaemon(true);
        thread.start();
    }

    public void queueOutMessage(RequestObject message) {
        log.info("queue: " + message.getCommand());
        if (message.getCommand().equals(AddPlayer.COMMAND)) {
            outMessages.removeIf(request -> request.getCommand().equals(AddPlayer.COMMAND));
            outMessages.addFirst(message);
        } else {
            outMessages.offer(message);
        }
        ev.set();
    }

    private boolean sendByTCP(RequestObject message) {
        Socket activeSocket = socket;
        BufferedWriter activeWriter = writer;
        if (activeSocket == null || activeSocket.isClosed() || activeWriter == null) {
            return false;
        }
        try {
            synchronized (activeWriter) {
                if (activeSocket.isClosed()) {
                    return false;
                }
                activeWriter.write(message.toJson());
                activeWriter.newLine();
                activeWriter.flush();
            }
            log.info("Sending to server: " + message.getCommand());
            return true;
        } catch (IOException ex) {
            log.error(ex.toString());
            disconnect(activeSocket);
            return false;
        }
    }

    private void listen(Socket connectedSocket, BufferedReader reader) {
        Thread thread = new Thread(() -> {
            try (BufferedReader input = reader) {
                String serverReply;
                while ((serverReply = input.readLine()) != null) {
                    if (!serverReply.isEmpty()) {
                        RequestObject message = RequestObject.fromString(serverReply);
                        if ("HeartbeatPong".equals(message.getCommand())) {
                            lastHeartbeatResponseAt = System.currentTimeMillis();
                            continue;
                        }
                        IInputputHandler inputHandler = client;
                        if (inputHandler != null) {
                            inputHandler.handleInput(message);
                        }
                    }
                }
                log.warn("Server closed the connection");
            } catch (IOException ex) {
                if (!connectedSocket.isClosed()) {
                    log.warn("Connection lost: " + ex);
                }
            } catch (RuntimeException ex) {
                log.error("Could not process server message: " + ex);
            } finally {
                disconnect(connectedSocket);
            }
        }, "client-message-reader");
        thread.setDaemon(true);
        thread.start();
    }

    private void connectLoop() {
        long reconnectDelay = INITIAL_RECONNECT_DELAY;
        while (!Thread.currentThread().isInterrupted()) {
            if (isSocketActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            wait.set(true);
            Socket newSocket = new Socket();
            try {
                newSocket.connect(new InetSocketAddress(hostname, port), CONNECT_TIMEOUT);
                newSocket.setKeepAlive(true);
                newSocket.setTcpNoDelay(true);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        newSocket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                        newSocket.getOutputStream(), StandardCharsets.UTF_8));

                output.write(new AddPlayer(name).toJson());
                output.newLine();
                output.flush();
                writer = output;
                socket = newSocket;
                wait.set(false);
                long connectedAt = System.currentTimeMillis();
                lastHeartbeatResponseAt = connectedAt;
                log.info("Connected to Server");
                listen(newSocket, input);
                ev.set();
                long lastHeartbeatSentAt = connectedAt;

                while (socket == newSocket && !newSocket.isClosed()
                        && !Thread.currentThread().isInterrupted()) {
                    long now = System.currentTimeMillis();
                    if (now - lastHeartbeatSentAt >= HEARTBEAT_INTERVAL) {
                        if (!sendHeartbeat(newSocket)) {
                            break;
                        }
                        lastHeartbeatSentAt = now;
                    }
                    if (now - lastHeartbeatResponseAt >= HEARTBEAT_TIMEOUT) {
                        log.warn("Heartbeat timed out");
                        disconnect(newSocket);
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    closeSocket(newSocket);
                    return;
                }
                if (System.currentTimeMillis() - connectedAt >= 30000) {
                    reconnectDelay = INITIAL_RECONNECT_DELAY;
                }
                sleepBeforeReconnect(reconnectDelay);
                reconnectDelay = Math.min(MAX_RECONNECT_DELAY, reconnectDelay * 2);
            } catch (IOException ex) {
                closeSocket(newSocket);
                log.warn("Could not connect to Server: " + ex);
                sleepBeforeReconnect(reconnectDelay);
                reconnectDelay = Math.min(MAX_RECONNECT_DELAY, reconnectDelay * 2);
            }
        }
    }

    private boolean sendHeartbeat(Socket connectedSocket) {
        BufferedWriter activeWriter = writer;
        if (socket != connectedSocket || activeWriter == null || connectedSocket.isClosed()) {
            return false;
        }
        try {
            synchronized (activeWriter) {
                activeWriter.write("{\"command\":\"HeartbeatPing\",\"params\":{}}");
                activeWriter.newLine();
                activeWriter.flush();
            }
            return true;
        } catch (IOException ex) {
            log.warn("Could not send heartbeat: " + ex);
            disconnect(connectedSocket);
            return false;
        }
    }

    private void sleepBeforeReconnect(long delay) {
        long jitter = ThreadLocalRandom.current().nextLong(Math.max(1, delay / 2));
        try {
            Thread.sleep(delay / 2 + jitter);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isSocketActive() {
        Socket activeSocket = socket;
        return activeSocket != null && activeSocket.isConnected() && !activeSocket.isClosed();
    }

    private void disconnect(Socket disconnectedSocket) {
        synchronized (this) {
            if (socket != disconnectedSocket) {
                closeSocket(disconnectedSocket);
                return;
            }
            socket = null;
            writer = null;
            wait.set(true);
        }
        closeSocket(disconnectedSocket);
        ev.set();
    }

    private void closeSocket(Socket socketToClose) {
        try {
            socketToClose.close();
        } catch (IOException ex) {
            log.warn("Could not close connection: " + ex);
        }
    }

    public void openTCPConnection() {
        wait.set(true);
        if (connectionLoopStarted.compareAndSet(false, true)) {
            Thread thread = new Thread(this::connectLoop, "client-connection-supervisor");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void setClient(BaseClient handler) {
        this.client = handler;
    }

    public void clearQueue() {
        outMessages.clear();
    }

    public boolean socketNotNull() {
        return isSocketActive();
    }

    public void start() {
        openTCPConnection();
        outMessageHandling();
    }
}
