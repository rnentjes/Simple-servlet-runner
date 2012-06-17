package nl.astraeus.http;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: rnentjes
 * Date: 5/7/12
 * Time: 10:16 PM
 */
public class ConnectionHandlerThread extends Thread {

    private BlockingQueue<ConnectionHandler> jobs;
    private volatile boolean running = false;
    private volatile boolean handling = false;
    private SimpleWebServer server;
    private long lastActivity;
    private ConnectionHandler currentJob;

    public ConnectionHandlerThread(SimpleWebServer server, String name, BlockingQueue<ConnectionHandler> jobs) {
        super(name);

        this.server = server;
        this.jobs = jobs;
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
                while (running) {
                    try {
                        currentJob = jobs.poll(1000, TimeUnit.MILLISECONDS);

                        if (currentJob != null) {
                            try {
                                handling = true;
                                lastActivity = System.currentTimeMillis();

                                currentJob.handle();
                            } finally {
                                handling = false;
                            }
                        }
                    } catch (InterruptedException e) {
                        // Expected
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
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

    public void close() {
        currentJob.close();
    }
}
