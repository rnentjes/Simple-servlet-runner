package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * User: rnentjes
 * Date: 12/11/12
 * Time: 8:03 PM
 */
public class AcceptorThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(AcceptorThread.class);

    boolean running;

    private ServerSocketChannel server;
    private Selector selector;

    private BlockingQueue<ConnectionCommand> queue;

    private Map<SocketChannel, ConnectionHandler> handlers = new HashMap<SocketChannel, ConnectionHandler>();

    public AcceptorThread(ServerSocketChannel server, BlockingQueue<ConnectionCommand> queue) {
        super("Acceptor thread");

        this.server = server;
        this.queue = queue;
    }

    public void run() {
        running = true;

        try {
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        while(running) {
            try {
                selector.select();

                Set keys = selector.selectedKeys();
                Iterator i = keys.iterator();

                while(i.hasNext()) {
                    SelectionKey key = (SelectionKey) i.next();

                    i.remove();

                    if (key.isAcceptable()) {
                        SocketChannel channel = server.accept();

                        if (channel != null) {
                            logger.info("Accepting connection from {}", channel.socket().toString());

                            ConnectionHandler handler = new ConnectionHandler();

                            handlers.put(channel, handler);

                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);

                            ConnectionCommand cmd = new ConnectionCommand(ConnectionCommand.Action.ACCEPT, handler);

                            queue.add(cmd);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        logger.info("Reading from {}", channel.socket().toString());

                        ConnectionHandler handler = handlers.get(channel);

                        if (channel != null && handler != null) {
                            handler.read(channel);

                            if (handler.isReadyToProcess()) {
                                ConnectionCommand cmd = new ConnectionCommand(ConnectionCommand.Action.PROCESS, handler);

                                queue.add(cmd);
                            }
                        } else {
                            throw new IllegalStateException("No handler found for channel: "+channel);
                        }
                     } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        logger.info("Writing to {}", channel.socket().toString());

                        ConnectionHandler handler = handlers.get(channel);

                        if (handler != null) {
                            handler.write(channel);
                        } else {
                            throw new IllegalStateException("No handler found for channel: "+channel);
                        }
                    }
                }
            } catch (CancelledKeyException e) {
                logger.warn(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
