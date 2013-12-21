package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
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

    private BlockingQueue<ConnectionHandler> queue;

    public AcceptorThread(int nr, ServerSocketChannel server, BlockingQueue<ConnectionHandler> queue) {
        super("Acceptor thread-"+nr);

        this.server = server;
        this.queue = queue;
    }

    public void run() {
        Selector selector;
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
                            logger.info("["+Thread.currentThread().getName()+"] Accepting connection from {}", channel.socket().toString());
                            channel.configureBlocking(false);

                            ConnectionHandler handler = new ConnectionHandler(channel, key);

                            handler.setCurrentKey(key);

                            offer(handler);
                        }
                    } else {
                        final ConnectionHandler handler = (ConnectionHandler)key.attachment();

                        synchronized (handler) {
                            if (handler.isReading() && key.isReadable()) {
                                handler.setCurrentKey(key);

                                offer(handler);
                            } else if (handler.isWriting() && key.isWritable()) {
                                handler.setCurrentKey(key);

                                offer(handler);
                            } else {
                                logger.warn("Ignoring key: "+key);
                            }
                        }
                    }
                }
            } catch (CancelledKeyException e) {
                logger.warn(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void offer(ConnectionHandler handler) {
        try {
            handler.process();
            //queue.offer(handler, 10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            logger.warn("Timeout while processing "+handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
