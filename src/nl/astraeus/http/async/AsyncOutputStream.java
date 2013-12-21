package nl.astraeus.http.async;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Date: 12/21/13
 * Time: 8:20 PM
 */
public class AsyncOutputStream extends OutputStream {

    private SocketChannel channel;
    private boolean buffered = true;
    private ByteBuffer out = ByteBuffer.allocate(4096);

    public AsyncOutputStream(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void write(int b) throws IOException {
        channel.write(ByteBuffer.wrap(new byte [] { (byte)b }));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        flush();

        channel.write(ByteBuffer.wrap(b, off, len));
    }

}
