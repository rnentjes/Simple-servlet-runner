package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: rnentjes
 * Date: 5/7/12
 * Time: 10:16 PM
 */
public class ConnectionHandlerThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionHandlerThread.class);

    private AsyncWebServer server;

    private BlockingQueue<ConnectionHandler> queue;
    private volatile boolean running = false;
    private volatile boolean handling = false;
    private long lastActivity;

    public ConnectionHandlerThread(int nr, AsyncWebServer server, BlockingQueue<ConnectionHandler> queue) {
        super("Connection handler-"+nr);

        this.server = server;
        this.queue = queue;
    }

    public boolean getRunning() {
        return running;
    }

    public boolean getHandling() {
        return handling;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean getAlive() {
        return isAlive();
    }

    @Override
    public void run() {
        running = true;

        try {
            try {
                ConnectionHandler currentJob;

                while (running) {
                    try {
                        currentJob = queue.poll(1000, TimeUnit.MILLISECONDS);

                        if (currentJob != null && !currentJob.isClosed()) {
                            try {
                                handling = true;
                                lastActivity = System.currentTimeMillis();

                                currentJob.process();
                            } catch (Exception e) {
                                logger.warn(e.getMessage(), e);
                            } finally {
                                handling = false;
                            }
                        }
                    } catch (InterruptedException e) {
                        // Expected
                    }
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            } finally {
                running = false;
                server.removeThread(this);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public long getLastActivity() {
        return lastActivity;
    }
}
