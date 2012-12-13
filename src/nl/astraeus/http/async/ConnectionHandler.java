package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 7:38 PM
 */
public class ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private static int BUFFER_SIZE = 1024;

    private ByteBuffer buffer;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private boolean readyToProcess = false;

    private SocketChannel channel;
    private Selector selector;

    public ConnectionHandler(SocketChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
    }

    public void accept() throws IOException {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buffer.rewind();
        channel.read(buffer);

        if (buffer.limit() > 3 &&
                buffer.array()[buffer.position()-1] == 10 &&
                buffer.array()[buffer.position()-2] == 13 &&
                buffer.array()[buffer.position()-3] == 10 &&
                buffer.array()[buffer.position()-4] == 13) {
            // done reading
            logger.info("Done reading the headers!!!");

            readyToProcess = true;
        }

        out.write(buffer.array(), 0,  buffer.position());
    }

    public void process() {
        logger.info("Processing!");
        readyToProcess = false;

        // todo: process

        try {
            SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE, this);
            key.selector().wakeup();
        } catch (ClosedChannelException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public boolean isReadyToProcess() {
        return readyToProcess;
    }

    public void write() throws IOException {
        // chunked 0
        out = new ByteArrayOutputStream();

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Transfer-Encoding: chunked\r\n".getBytes());
        out.write("\r\n".getBytes());

        byte [] chunk = "This is a test".getBytes();

        out.write((Integer.toHexString(chunk.length)+"\r\n").getBytes());
        out.write(chunk);
        out.write("\r\n".getBytes());

        out.write("0\r\n\r\n".getBytes());

        logger.info(out.toString());

        channel.write(ByteBuffer.wrap(out.toByteArray()));

        channel.close();

//        try {
//            SelectionKey key = channel.register(selector, SelectionKey.OP_READ, this);
//            key.selector().wakeup();
//        } catch (ClosedChannelException e) {
//            logger.warn(e.getMessage(), e);
//        }
    }

}
