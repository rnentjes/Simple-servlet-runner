package nl.astraeus.http.async;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: rnentjes
 * Date: 12/11/12
 * Time: 7:37 PM
 */
public class AsyncWebServer {
    private boolean                          running           = false;
    private boolean                          started           = false;
    private int                              port              = 8080;

    private int                              numberOfAcceptors = 2;
    private int                              numberOfHandlers  = Runtime.getRuntime().availableProcessors();

    private Set<AcceptorThread>              acceptorThreads   = new HashSet<AcceptorThread>();
    private Set<ConnectionHandlerThread>     handlerThreads    = new HashSet<ConnectionHandlerThread>();

    private BlockingQueue<ConnectionCommand> queue             = new LinkedBlockingQueue<ConnectionCommand>(numberOfHandlers);

    public AsyncWebServer(int port) {
        this.port = port;
    }

    public void removeThread(ConnectionHandlerThread connectionHandlerThread) {
    }

    public void setNumberOfAcceptorThreads(int numberOfAcceptors) {
        this.numberOfAcceptors = numberOfAcceptors;
    }

    public void setNumberOfHandlerThreads(int numberOfHandlers) {
        this.numberOfHandlers = numberOfHandlers;
    }

    public void start() throws IOException {
        // Create the server socket channel
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);

        server.socket().bind(new java.net.InetSocketAddress("0.0.0.0", port));
        System.out.println("Server listening on port "+port);

        for (int i=0; i < numberOfAcceptors; i++) {
            acceptorThreads.add(new AcceptorThread(server, queue));
        }

        for (int i=0; i < numberOfHandlers; i++) {
            handlerThreads.add(new ConnectionHandlerThread(this, queue));
        }

        for (AcceptorThread acceptor : acceptorThreads) {
            acceptor.start();
        }

        for (ConnectionHandlerThread handler : handlerThreads) {
            handler.start();
        }

        started = true;
    }

    public static void main(String [] args) throws IOException {
        AsyncWebServer server = new AsyncWebServer(8080);

        server.start();
    }
}
