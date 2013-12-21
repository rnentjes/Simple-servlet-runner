package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 7:38 PM
 */
public class ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    public enum ConnectionStatus {
        NEW,
        ACCEPTING,
        READING,
        WRITING,
        CLOSED
    }

    private ByteBuffer in = ByteBuffer.allocate(4096);
    private ByteBuffer out = ByteBuffer.allocate(4096);
    private ConnectionStatus status = ConnectionStatus.NEW;

    private SocketChannel channel;
    private SelectionKey currentKey;
    private int headerPositionSearch = 0;

    private final long id;

    private static long nextId = 0L;
    private static synchronized long nextId() {
        return ++nextId;
    }

    public ConnectionHandler(SocketChannel channel, SelectionKey key) {
        this.id = nextId();
        this.currentKey = key;
        this.channel = channel;
        this.status = ConnectionStatus.ACCEPTING;
    }

    public void setCurrentKey(SelectionKey currentKey) {
        this.currentKey = currentKey;
    }

    public boolean isAccepting() {
        return status == ConnectionStatus.ACCEPTING;
    }

    public boolean isReading() {
        return status == ConnectionStatus.READING;
    }

    public boolean isWriting() {
        return status == ConnectionStatus.WRITING;
    }

    public boolean isClosed() {
        return status == ConnectionStatus.CLOSED;
    }

    public void accept() throws IOException {
        logger.info("Accept");

        currentKey.attach(this);

        setStatus(ConnectionStatus.READING);
    }

    private void setStatus(ConnectionStatus status) {
        this.status = status;
        int interestedOps = 0;

        switch(status) {
            case ACCEPTING:
                interestedOps = SelectionKey.OP_ACCEPT;
                break;
            case READING:
                interestedOps = SelectionKey.OP_READ;
                break;
            case WRITING:
                interestedOps = SelectionKey.OP_WRITE;
                break;
            case CLOSED:
                return;
        }

        try {
            currentKey.selector().wakeup();

            if (currentKey.selector().isOpen()) {
                SelectionKey key = channel.register(currentKey.selector(), interestedOps, this);
                key.selector().wakeup();
            } else {
                logger.warn("Selector is closed: " + currentKey.selector());
            }
        } catch (ClosedChannelException e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public void process() throws IOException {
        logger.info("[Handler-"+id+"] ["+Thread.currentThread().getName()+"] Processing! ["+status+"]");

        switch(status) {
            case ACCEPTING:
                accept();
                break;
            case READING:
                read();
                break;
            case WRITING:
                write();
                break;
            case CLOSED:
                logger.warn("Trying to process closed connection!");
                break;
        }
    }

    private void parseIncomingRequest() {
        logger.info("Parsing");
    }

    public void read() throws IOException {
        logger.info("Reading");
        boolean found = false;
        SocketChannel channel = (SocketChannel)currentKey.channel();
        int nr = channel.read(in);

        if (nr < 0) {
            logger.warn("Partial read! [" + new String(out.array(), 0, out.position(), "UTF-8") + "]");

            close();

            return;
        } else if (nr == 0) {
            logger.info("Empty read");

            return;
        }

        // find the end of the headers
        while(headerPositionSearch < in.position()) {
            if (in.get(headerPositionSearch) == 10) {
                 if (in.get(headerPositionSearch-1) == 13 &&
                     in.get(headerPositionSearch-2) == 10 &&
                     in.get(headerPositionSearch-3) == 13) {
                     found = true;
                     break;
                 } else {
                     headerPositionSearch++;
                 }
            } else if (in.get(headerPositionSearch) == 13) {
                headerPositionSearch++;
            } else {
                headerPositionSearch += 4;
            }
        }

        if (found) {
            // done reading
            logger.info("Done reading the headers!!!");

            parseIncomingRequest();

            // rewind for next read
            rewindIn();

            setStatus(ConnectionStatus.WRITING);
        } else {
            // read more
            setStatus(ConnectionStatus.READING);
        }
    }

    public void write() throws IOException {
        logger.info("Writing");
        // chunked 0

        out.put("HTTP/1.1 200 OK\r\n".getBytes());
        out.put("Content-type: text/plain\r\n".getBytes());
        out.put("Transfer-Encoding: chunked\r\n".getBytes());
        out.put("\r\n".getBytes());

        byte [] chunk = "This is a test\n\n".getBytes();

        out.put((Integer.toHexString(chunk.length) + "\r\n").getBytes());
        out.put(chunk);
        out.put("\r\n".getBytes());

        out.put("0\r\n\r\n".getBytes());

        logger.info(new String(out.array(), 0, out.position(), "UTF-8"));

        channel.write(out);

        out.rewind();

        setStatus(ConnectionStatus.READING);
    }

    private void close() {
        try {
            SocketChannel channel = (SocketChannel)currentKey.channel();
            channel.close();
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }

        setStatus(ConnectionStatus.CLOSED);
    }

    private void rewindIn() {
        in.rewind();
        headerPositionSearch = 0;
    }

}
