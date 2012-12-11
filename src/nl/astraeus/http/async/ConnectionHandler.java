package nl.astraeus.http.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 7:38 PM
 */
public class ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private static int BUFFER_SIZE = 1024;

    public static enum ConnectionStatus {
        INIT,
        ACCEPTING,
        READING,
        READY_TO_PROCESS,
        PROCESSING,
        READY_TO_WRITE,
        WRITING,
        CLOSING,
        WAITING
    }

    private ConnectionStatus status = ConnectionStatus.INIT;

    private ByteBuffer buffer;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    public ConnectionStatus getStatus() {
        return status;
    }

    public void accept() throws IOException {
        status = ConnectionStatus.ACCEPTING;
        try {
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
        } finally{
            status = ConnectionStatus.WAITING;
        }
    }

    public void read(SocketChannel channel) throws IOException {
        status = ConnectionStatus.READING;
        try {
            buffer.rewind();
            channel.read(buffer);

            if (buffer.limit() > 3 &&
                    buffer.array()[buffer.position()-1] == 10 &&
                    buffer.array()[buffer.position()-2] == 13 &&
                    buffer.array()[buffer.position()-3] == 10 &&
                    buffer.array()[buffer.position()-4] == 13) {
                // done reading
                logger.info("Done reading the headers!!!");
                status = ConnectionStatus.READY_TO_PROCESS;
            }

            out.write(buffer.array(), 0,  buffer.position());

            // Show bytes on the console
            //buffer.flip();
//            Tank tank = clients.get(client);
//            if (tank != null && (buffer.limit()-buffer.position()) > 0) {
//                tank.onMessage(buffer.array(), buffer.position(), buffer.limit());
//            }

        } finally{
            status = ConnectionStatus.WAITING;
        }
    }

    public void process() {
        logger.info("Processing!");


    }

    public boolean isReadyToProcess() {
        return status == ConnectionStatus.READY_TO_PROCESS;
    }

    public void write(SocketChannel channel) throws IOException {
        status = ConnectionStatus.WRITING;
        try {

        } finally{
            status = ConnectionStatus.WAITING;
        }
    }

    public void close() throws IOException {
        status = ConnectionStatus.CLOSING;
        try {

        } finally{
            status = ConnectionStatus.WAITING;
        }
    }

}
