package nl.astraeus.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.*;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 7:38 PM
 */
public class ConnectionHandler {

    private final static int LINE_BUFFER_SIZE = 1024;

    private static final String HTTP_1_0 = "HTTP/1.0";
    private static final String HTTP_1_1 = "HTTP/1.1";

    private static int OUT_BUFFER = 1 << 16;
    private static int IN_BUFFER  = 1 << 14;

    private SocketChannel sc;
    private SimpleWebServer server;

    private Charset isoCharset = Charset.forName("ISO-8859-1");
    private static Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();
    private CharsetDecoder decoder = charset.newDecoder();

    private boolean running = true;
    private boolean keepalive = true;
    private int requestNumber = 1;
    private Charset utf8 = Charset.forName("UTF-8");

    private char [] charbuffer = new char[LINE_BUFFER_SIZE];
    private byte [] linebuffer = new byte[LINE_BUFFER_SIZE];
    private CharBuffer chars = CharBuffer.allocate(LINE_BUFFER_SIZE);
    private char [] chararray = chars.array();
    ByteBuffer inBuffer = ByteBuffer.allocate(IN_BUFFER);
    private byte [] inarray = inBuffer.array();


    public ConnectionHandler(SimpleWebServer server, SocketChannel sc) {
        this.server = server;
        this.sc = sc;
    }

    private List<String> readBlock(ByteBuffer in, SocketChannel sc) throws IOException {
        List<String> result = new ArrayList<String>();
        String line;

        while(!"".equals((line = nextLine2(in, sc)))) {
            result.add(line);
        }

        return result;
    }

    //private byte [] getHeader = "GET ".getBytes(charset);
    //private byte [] postHeader = "POST ".getBytes(charset);
    private static byte [] contentLengthHeader = "_ontent-_ength: ".getBytes(charset);
    private static byte [] connectionHeader    = "_onnection: ".getBytes(charset);
    private static byte [] cookieHeader        = "_ookie: ".getBytes(charset);
    private static byte [] contentTypeHeader   = "_ontent-_ype: ".getBytes(charset);

    private static Map<HttpHeader, byte[]> headers;

    static {
        //headers.put(HttpHeader.GET, getHeader);
        //headers.put(HttpHeader.POST, postHeader);
        headers = new HashMap<HttpHeader, byte[]>();

        headers.put(HttpHeader.CONTENT_LENGTH, contentLengthHeader);
        headers.put(HttpHeader.CONTENT_TYPE, contentTypeHeader);
        headers.put(HttpHeader.COOKIE, cookieHeader);
        headers.put(HttpHeader.CONNECTION, connectionHeader);
    }

    private Map.Entry<HttpHeader, String> findHeader2(byte[] bytes, int offset, int len) {
        Map.Entry<HttpHeader, String> result = null;

        int index = 0;
        Set<HttpHeader> remaining = new HashSet<HttpHeader>(headers.keySet());

        while (index < len && remaining.size() > 1) {
            Iterator<HttpHeader> it = remaining.iterator();
            byte currentByte = bytes[offset+index];

            while (it.hasNext()) {
                HttpHeader header = it.next();
                byte [] headerBytes = headers.get(header);

                if (currentByte != headerBytes[index] && headerBytes[index] != '_') {
                    it.remove();
                }
            }

            index++;
        }

        if (remaining.size() == 1) {
            HttpHeader header = remaining.iterator().next();
            byte [] headerBytes = headers.get(header);

            String value = new String(bytes, offset + headerBytes.length, len - headerBytes.length, charset);
            result = new AbstractMap.SimpleEntry<HttpHeader, String>(header, value);
        }

        return result;
    }

    private String checkMatch(byte[] bytes, int offset, int len, byte [] target) {
        String result = null;
        int index = 0;
        boolean match = true;

        while(match && index < len && index < target.length) {
            match = bytes[offset+index] == target[index] || target[index] == '_';
            index++;
        }

        if (match) {
            result = new String(bytes, offset + index, len - index, charset);
        }

        return result;
    }

    private Map.Entry<HttpHeader, String> findHeader(byte[] bytes, int offset, int len) {
        Map.Entry<HttpHeader, String> result = null;
        String test = new String(bytes, offset, len, charset);
        String header = null;
        int index = 0;
        boolean match = false;

        if (len > 5) {
            if (bytes[offset+3] == 'k') {
                header = checkMatch(bytes, offset, len, cookieHeader);
                result = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.COOKIE, header);
            }

            if (header == null && len > 9) {
                if (bytes[offset+9] == 'n') {
                    header = checkMatch(bytes, offset, len, connectionHeader);
                    result = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.CONNECTION, header);
                }

                if (header == null && bytes[offset+9] == 'y') {
                    header = checkMatch(bytes, offset, len, contentTypeHeader);
                    result = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.CONTENT_TYPE, header);
                }

                if (header == null && bytes[offset+12] == 't') {
                    header = checkMatch(bytes, offset, len, contentLengthHeader);
                    result = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.CONTENT_LENGTH, header);
                }
            }
        }

        if (header == null) {
            result = null;
        }

        return result;
    }

    private String createString(byte [] bytes, int offset, int len) {
        return new String(bytes, offset, len, charset);
    }

    private int readSomeMore(ByteBuffer in) throws IOException {
        int result;

        in.clear();
        result = sc.read(in);
        in.flip();

        return result;
    }

    private Map<HttpHeader, String> readHeaders(ByteBuffer in, SocketChannel sc) throws IOException {
        boolean done = false;
        boolean first = true;
        Map<HttpHeader, String> headers = new HashMap<HttpHeader, String>();

        while(!done) {
            boolean found = false;

            while (!found) {
                Map.Entry<HttpHeader, String> header = null;
                if (!in.hasRemaining()) {
                    int bytes = readSomeMore(in);

                    if (bytes == -1) {
                        found = true;
                        done = true;
                    }
                }

                if (!in.hasRemaining()) {
                    found = true;
                }

                for (int p = inBuffer.position(); !found && p < inBuffer.limit(); p++) {
                    if (p > inBuffer.position() && inarray[p-1] == '\r' && inarray[p] == '\n') {
                        // found one line
                        found = true;

                        if ((inBuffer.position() - p) == -1) {
                            inBuffer.position(p+1);
                            done = true;
                        } else if (first) {
                            String postGet = createString(inarray, inBuffer.position(), (p - inBuffer.position()) -1);
                            if (postGet.startsWith("GET")) {
                                header = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.GET, postGet.substring(4));
                            } else if (postGet.startsWith("POST")) {
                                header = new AbstractMap.SimpleEntry<HttpHeader, String>(HttpHeader.POST, postGet.substring(5));
                            }
                            first = false;
                        } else {
                            header = findHeader2(inarray, inBuffer.position(), (p - inBuffer.position()) - 1);
                        }

                        inBuffer.position(p + 1);
                    }
                }

                /* headers need to fit in only read....
                if (!found) {
                    result.append(new String(inarray, inBuffer.position(), inBuffer.limit(), charset));
                    inBuffer.position(inBuffer.limit());
                }*/

                if (found) {
                    if (header != null) {
                        headers.put(header.getKey(), header.getValue());
                    }
                }
            }
        }

        return headers;
    }

    private String [] readBlock2(ByteBuffer in, SocketChannel sc) throws IOException {
        boolean done = false;
        int bytes = -1;
        StringBuilder response = new StringBuilder();
        chars.clear();

        while(!done && (bytes = sc.read(in)) != -1) {
            in.flip();

            decoder.decode(in, chars, false);

            chars.flip();

            String tmp = new String(chararray, chars.position(), chars.remaining());

            if (tmp.endsWith("\r\n\r\n")) {
                done = true;
            }

            in.compact();
            chars.clear();

            response.append(tmp);
        }

        return response.toString().split("\r\n");
    }

    private String readRemaining(ByteBuffer in, SocketChannel sc)throws IOException {
        int bytes = -1;
        StringBuilder response = new StringBuilder();
        boolean found = false;
        boolean first = true;

        while (in.hasRemaining()) {
            decoder.decode(in).toString();
            char ch = (char) in.get();

            response.append(ch);
        }

        return response.toString();
    }

    private String nextLineOld(ByteBuffer in, SocketChannel sc) throws IOException {
        int bytes = -1;
        StringBuilder response = new StringBuilder();
        boolean found = false;

        while (!found) {
            if (!in.hasRemaining()) {
                in.clear();
                bytes = sc.read(in);
                in.flip();
            }

            if (in.hasRemaining()) {
                char lastchar = ' ';

                while(in.hasRemaining() && !found) {
                    char ch = (char) in.get();

                    if (ch != '\r' && ch != '\n') {
                        response.append(ch);
                    } else if (ch == '\n' && lastchar == '\r') {
                        found = true;
                    }

                    lastchar = ch;
                }
            } else {
                response = new StringBuilder();
                response.append("");
                found = true;
            }
        }

        return response.toString();
    }

    private String nextLine(ByteBuffer in, SocketChannel sc) throws IOException {
        boolean found = false;
        int index = 0;
        String result = "";

        while (!found) {
            if (!in.hasRemaining()) {
                in.clear();
                sc.read(in);
                in.flip();
            }

            if (!in.hasRemaining()) {
                found = true;
            }

            while (!found && in.hasRemaining() && index < LINE_BUFFER_SIZE) {
                charbuffer[index++] = (char)in.get();

                if (index > 1 && charbuffer[index-2] == '\r' && charbuffer[index-1] == '\n') {
                    result = new String(charbuffer); //linebuffer, 0, index-2, charset);
                    found = true;
                }
            }
        }

        return result;
    }

    //inarray
    //String tmp = new String(inarray, inBuffer.position(), inBuffer.position()+index)
    // inBuffer setPosition();
    private String nextLine2(ByteBuffer in, SocketChannel sc) throws IOException {
        boolean found = false;
        StringBuilder result = new StringBuilder();

        while (!found) {
            int index = 0;
            if (!in.hasRemaining()) {
                in.clear();
                sc.read(in);
                in.flip();
            }

            if (!in.hasRemaining()) {
                found = true;
            }

            for (int p = inBuffer.position(); !found && p < inBuffer.limit(); p++) {
                linebuffer[index++] = inarray[p];
                if (p > inBuffer.position() && inarray[p-1] == '\r' && inarray[p] == '\n') {
                    // found one line
                    found = true;

                    result.append(new String(inarray, inBuffer.position(), (p - inBuffer.position()) -1, isoCharset));

                    inBuffer.position(p + 1);

                }
            }

            if (!found) {
                result.append(new String(inarray, inBuffer.position(), inBuffer.limit(), charset));
                inBuffer.position(inBuffer.limit());
            }
        }

        return result.toString();
    }

    //inarray
    //String tmp = new String(inarray, inBuffer.position(), inBuffer.position()+index)
    // inBuffer setPosition();
    private String nextLine3(ByteBuffer in, SocketChannel sc) throws IOException {
        boolean found = false;
        StringBuilder result = new StringBuilder();

        while (!found) {
            if (!in.hasRemaining()) {
                in.clear();
                sc.read(in);
                in.flip();
            }

            if (!in.hasRemaining()) {
                found = true;
            }

            for (int p = inBuffer.position(); !found && p < inBuffer.limit(); p++) {
                if (p > inBuffer.position() && inarray[p-1] == '\r' && inarray[p] == '\n') {
                    // found one line
                    found = true;

                    int oldLimit = inBuffer.limit();

                    inBuffer.limit(p-1);

                    chars.clear();

                    decoder.decode(in, chars, false);

                    chars.flip();

                    String tmp = new String(chararray, chars.position(), chars.remaining());

                    result.append(tmp);
                    inBuffer.limit(oldLimit);
                    inBuffer.position(p + 1);
                }
            }

            if (!found) {
                result.append(new String(inarray, inBuffer.position(), inBuffer.limit(), charset));
                inBuffer.position(inBuffer.limit());
            }
        }

        return result.toString();
    }

    void readMultiPartFormData(ByteBuffer in, SocketChannel sc, SimpleHttpRequest request) {
        int size = request.getContentLength();

        String boundary = request.getContentType().substring("multipart/form-data; boundary=".length());

        byte [] boundaryBytes = boundary.getBytes(charset);

        int boundaryMatchIndex = 0;



    }



    private String readCharacters(ByteBuffer in, SocketChannel sc, int size) throws IOException {
        byte [] result = new byte[size];
        int index = 0;

        while (index < size) {
            if (in.hasRemaining()) {
                result[index++] = in.get();
            } else {
                in.clear();
                int bytes = sc.read(in);
                in.flip();

                if (bytes == -1) {
                    break;
                }
            }
        }

        return new String(result, charset);
    }

    public void handle() {
        try {
            ByteBuffer outBuffer = ByteBuffer.allocate(OUT_BUFFER);

            inBuffer.clear();
            inBuffer.flip();
            outBuffer.clear();

            while (running && keepalive) {
                Map<HttpHeader, String> headers = readHeaders(inBuffer, sc);

                keepalive = false;

                long requestStart = System.nanoTime();

                SimpleHttpRequest request = null;
                SimpleHttpResponse response = null;

                if (!headers.isEmpty()) {
                    String in;
                    if ((in = headers.get(HttpHeader.GET)) != null) {
                        if (in.endsWith(HTTP_1_1)) {
                            request = new SimpleHttpRequest(server, HttpMethod.GET, in.substring(0, in.length() - HTTP_1_1.length()), true);
                            request.readHeaders(headers);
                        } else if (in.endsWith(HTTP_1_0)) {
                            request = new SimpleHttpRequest(server, HttpMethod.GET, in.substring(0, in.length() - HTTP_1_0.length()), false);
                            request.readHeaders(headers);
                        } else {
                            throw new IllegalStateException("Don't know how to handle: [" + in + "]");
                        }
                    } else if ((in = headers.get(HttpHeader.POST)) != null) {
                        if (in.endsWith(HTTP_1_1)) {
                            request = new SimpleHttpRequest(server, HttpMethod.POST, in.substring(0, in.length() - HTTP_1_1.length()), true);
                        } else if (in.endsWith(HTTP_1_0)) {
                            request = new SimpleHttpRequest(server, HttpMethod.POST, in.substring(0, in.length() - HTTP_1_0.length()), false);
                        } else {
                            throw new IllegalStateException("Don't know how to handle: [" + in + "]");
                        }

                        request.readHeaders(headers);
                        if (request.isMultiPartFormData()) {
                            readMultiPartFormData(inBuffer, sc, request);
                        } else {
                            request.parseRequestParameters(readCharacters(inBuffer, sc, request.getContentLength()));
                        }
                    }
                }

                if (request == null) {
                    response = new SimpleHttpResponse(server);
                    response.sendError(404, "Unknown request type (only GET supported!)");
                } else {
                    keepalive = request.getKeepAlive();

                    HttpServlet servlet = server.findHandlingServlet(request.getRequestURI());

                    if (servlet == null) {
                        response = new SimpleHttpResponse(server);
                        response.sendError(404, "No handler defined for: " + request.getRequestURI());
                    } else {
                        response = new SimpleHttpResponse(server);

                        SimpleHttpSession session = (SimpleHttpSession)request.getSession();

                        if (session.getLastAccessedTime() < (System.currentTimeMillis() - (session.getMaxInactiveInterval() * 1000L))) {
                            // new session
                        } else {
                            session.setLastAccessedTime();
                        }

                        servlet.service(request, response);

                        try {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();

                            response.writeToOutputStream(request, new DataOutputStream(out));

                            writeBytesToChannel(out.toByteArray(), outBuffer, sc);
                            writeBytesToChannel(((SimpleServletOutputStream)response.getOutputStream()).getBytes(), outBuffer, sc);
                        } catch (SocketException e) {
                            // connection reset by client, ignoring
                        }

                        server.addRequestLog(servlet, request, (System.nanoTime() - requestStart));
                    }
                }
            }
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sc != null) {
                    sc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeBytesToChannel(byte [] bytes, ByteBuffer outBuffer, SocketChannel sc) throws IOException {
        int offset = 0;

        while(offset < bytes.length) {
            int length = OUT_BUFFER;

            if ((offset + length) > bytes.length) {
                length = bytes.length - offset;
            }

            outBuffer.clear();
            outBuffer.put(bytes, offset, length);

            outBuffer.flip();

            while(outBuffer.hasRemaining()) {
                sc.write(outBuffer);
            }

            outBuffer.flip();

            offset += OUT_BUFFER;
        }

    }

}
