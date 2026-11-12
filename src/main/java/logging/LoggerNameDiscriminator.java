package logging;

import base.Constants;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;

public class LoggerNameDiscriminator implements Discriminator<ILoggingEvent> {

    private static final int MAX_CLIENT_NAME_LENGTH = 80;
    private boolean started;

    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        if (Constants.SERVER.equals(loggerName)) {
            return Constants.SERVER;
        }
        if (loggerName.startsWith(Constants.CLIENT)) {
            String clientName = loggerName.substring(Constants.CLIENT.length())
                    .replaceAll("[^A-Za-z0-9_-]", "_");
            if (clientName.length() > MAX_CLIENT_NAME_LENGTH) {
                clientName = clientName.substring(0, MAX_CLIENT_NAME_LENGTH);
            }
            return "client-" + (clientName.isEmpty() ? "unknown" : clientName);
        }
        return "application";
    }

    @Override
    public String getKey() {
        return "logFile";
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
