package nl.astraeus.http;

import org.junit.Ignore;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * User: rnentjes
 * Date: 12/14/12
 * Time: 8:13 PM
 */
@Ignore
public class SocketResponse {

    private byte [] data;

    public SocketResponse() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Transfer-Encoding: chunked\r\n".getBytes());
        out.write("\r\n".getBytes());

        byte [] chunk = "This is a test".getBytes();

        out.write((Integer.toHexString(chunk.length)+"\r\n").getBytes());
        out.write(chunk);
        out.write("\r\n".getBytes());

        out.write("0\r\n\r\n".getBytes());

        data = out.toByteArray();
    }

    public void start() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress isa = new InetSocketAddress(8080);
        ssc.socket().bind(isa);

        ByteBuffer buffer = ByteBuffer.allocate(16000);
        ByteBuffer bufferOut = null;

        while(true) {
            bufferOut = ByteBuffer.wrap(data);
            SocketChannel sc = ssc.accept();
            sc.socket().setTcpNoDelay(true);

            sc.read(buffer);

            sc.write(bufferOut);
            sc.close();
        }
    }


    public static void main(String [] args) throws Exception {
        new SocketResponse().start();
    }
}
