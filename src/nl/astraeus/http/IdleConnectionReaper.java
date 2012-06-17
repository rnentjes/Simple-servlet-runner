package nl.astraeus.http;

/**
 * User: rnentjes
 * Date: 6/17/12
 * Time: 2:15 PM
 */
public class IdleConnectionReaper implements Runnable {
    private SimpleWebServer server;
    private boolean running;

    public IdleConnectionReaper(SimpleWebServer server) {
        this.server = server;
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        running = true;

        while(running) {
            try {
                Thread.sleep(1000);
                long killTime = System.currentTimeMillis() - 10000;

                for(ConnectionHandlerThread handler : server.getThreads()) {
                    if (handler.getHandling() && handler.getLastActivity() < killTime) {
                        handler.close();
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
