package nl.astraeus.http.async;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * User: rnentjes
 * Date: 12/11/12
 * Time: 8:47 PM
 */
public class ConnectionCommand {

    private SocketChannel channel;
    private Selector selector;
    private ConnectionHandler handler;

    public ConnectionCommand(SocketChannel channel, Selector selector, ConnectionHandler handler) {
        this.channel = channel;
        this.selector = selector;
        this.handler = handler;
    }

    public void run() throws IOException {
        handler.process();
    }

}
