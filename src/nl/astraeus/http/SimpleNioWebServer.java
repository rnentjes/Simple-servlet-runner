package nl.astraeus.http;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 7:36 PM
 */
public class SimpleNioWebServer extends SimpleWebServer implements Runnable {


    private Selector selector;

    public SimpleNioWebServer(int port) {
        super(port);
    }

    public void run() {
        try {
            selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            InetSocketAddress isa = new InetSocketAddress(port);
            ssc.socket().bind(isa);

            ssc.register(selector, SelectionKey.OP_ACCEPT);

            try {
                while (running) {
                    selector.select();

                    // Iterate over the set of keys for which events are available
                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        // Check what event is available and deal with it
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            //read(key);
                        }
                    }


                    //SocketChannel sc = ssc.accept();

                    //ConnectionHandler handler = new ConnectionHandler(this, sc);

                    //handleConnection(handler);
                }
            } finally {
                ssc.close();//always close the ServerSocket
            }
        } catch (BindException B) {
            //handling exception generated if they are already running server
            System.out.println("SERVER Already Running");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(selector, SelectionKey.OP_READ);
    }


}
