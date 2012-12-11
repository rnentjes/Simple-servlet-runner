package nl.astraeus.http.async;

import java.io.IOException;

/**
 * User: rnentjes
 * Date: 12/11/12
 * Time: 8:47 PM
 */
public class ConnectionCommand {

    public static enum Action {
        ACCEPT,
        PROCESS,
        CLOSE
    }

    private Action action;
    private ConnectionHandler handler;

    public ConnectionCommand(Action action, ConnectionHandler handler) {
        this.action = action;
        this.handler = handler;
    }

    public void run() throws IOException {
        switch(action) {
            case ACCEPT:
                handler.accept();
                break;
            case PROCESS:
                handler.process();
                break;
            case CLOSE:
                handler.close();
                break;
        }
    }
}
