package nl.astraeus.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 8:52 AM
 */
public class SimpleHttpResponse implements HttpServletResponse {
    private SimpleServletOutputStream outputStream;
    private PrintWriter printWriter;
    private int responseCode;
    private String errorMessage;
    private Map<String, String[]> headers;
    private Set<Cookie> cookies;
    private Map<Integer, String> responseMessages;
    private SimpleWebServer server;

    private String contentType;
    private String redirect = null;

    public SimpleHttpResponse(SimpleWebServer server) {
        this.server = server;
        this.outputStream = new SimpleServletOutputStream();
        this.printWriter = null;
        this.responseCode = 200;
        this.errorMessage = "";

        this.contentType = "text/html; charset=utf-8";

        this.headers = new HashMap<String, String[]>();
        this.cookies = new HashSet<Cookie>();
        this.responseMessages = new HashMap<Integer, String>();

        responseMessages.put(200, "OK");
        responseMessages.put(400, "Bad Request");
        responseMessages.put(401, "Unauthorized");
        responseMessages.put(403, "Forbidden");
        responseMessages.put(404, "Not Found");
    }

    void writeToOutputStream(SimpleHttpRequest request, DataOutputStream output) throws IOException {
        if (request != null && request.getHttp11()) {
            output.writeBytes("HTTP/1.1 ");
        } else {
            output.writeBytes("HTTP/1.0 ");
        }

        if (redirect != null) {
            output.writeBytes("302 Redirect\r\n");
            output.writeBytes("Location: ");
            output.writeBytes(redirect);
            output.writeBytes("\r\n");
        } else {
            output.writeBytes(Integer.toString(responseCode));
            output.writeBytes(" ");

            if (responseCode != 200) {
                setContentType("text/plain");
                resetBuffer();
                getWriter().print(errorMessage);
            } else {
                output.writeBytes("OK");
                output.writeBytes("\r\n");
            }

            output.writeBytes("Content-Type: ");
            output.writeBytes(getContentType());
            output.writeBytes("\r\n");

            //Set-Cookie: name2=value2; Expires=Wed, 09 Jun 2021 10:18:14 GMT

            if (request != null && request.getSessionId() != null) {
                output.writeBytes("Set-Cookie: ");
                output.writeBytes(SimpleHttpRequest.SESSION_COOKIE);
                output.writeBytes("=");
                output.writeBytes(request.getSessionId());

                output.writeBytes("\r\n");
            }

            SimpleDateFormat expiresFormatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
            expiresFormatter.setTimeZone(new SimpleTimeZone(0, "GMT"));

            for (Cookie cookie : cookies) {
                output.writeBytes("Set-Cookie: ");
                output.writeBytes(cookie.getName());
                output.writeBytes("=");
                output.writeBytes(cookie.getValue());

                if (cookie.getMaxAge() > -1) {
                    Date expires = new Date(System.currentTimeMillis() + cookie.getMaxAge() * 1000L);
                    output.writeBytes("; Expires=" + expiresFormatter.format(expires));
                }

                output.writeBytes("\r\n");
            }
        }

        if (request != null && request.getKeepAlive() && server.isSupportKeepAlive()) {
            output.writeBytes("Connection: keep-alive\r\n");
        }

        for (Map.Entry<String, String[]> entry : headers.entrySet()) {
            output.writeBytes(entry.getKey());
            output.writeBytes(": ");
            boolean first = true;

            for (String value : entry.getValue()) {
                if (!first) {
                    output.writeBytes(";");
                } else {
                    first = false;
                }

                output.writeBytes(value);
            }
            output.writeBytes("\r\n");
        }

        output.writeBytes("Content-Length: ");
        output.writeBytes(Integer.toString(outputStream.length()));
        output.writeBytes("\r\n");
        output.writeBytes("\r\n");
    }

    private String getResponseMessage() {
        String result = responseMessages.get(responseCode);

        if (result == null) {
            result = "Internal Server Error";
        }

        return result;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public boolean containsHeader(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeURL(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeRedirectURL(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeUrl(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeRedirectUrl(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendError(int i, String s) throws IOException {
        responseCode = i;
        errorMessage = s;
    }

    public void sendError(int i) throws IOException {
        responseCode = i;
        errorMessage = getResponseMessage();
    }

    public void sendRedirect(String s) throws IOException {
        redirect = s;
    }

    public void setDateHeader(String s, long l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addDateHeader(String s, long l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setHeader(String s, String s1) {
        String [] value = new String[1];
        value[0] = s1;
        headers.put(s, value);
    }

    public void addHeader(String s, String s1) {
        String [] values = headers.get(s);

        if (values == null) {
            values = new String[1];
            values[0] = s1;
            headers.put(s, values);
        } else {
            String [] newValues = Arrays.copyOf(values, values.length+1);
            newValues[newValues.length-1] = s1;
            headers.put(s, newValues);
        }
    }

    public void setIntHeader(String s, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addIntHeader(String s, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStatus(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStatus(int i, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCharacterEncoding() {
        return "UTF-8";
    }

    public String getContentType() {
        return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(new SimpleUTF8Writer(outputStream));
        }

        return printWriter;
    }

    public void setCharacterEncoding(String s) {
        throw new IllegalStateException("Changing encoding is not allowed, encoding is hardcoded to UTF-8.");
    }

    public void setContentLength(int i) {
        throw new IllegalStateException("Setting content length not supported (it's calculated)");
    }

    public void setContentType(String s) {
        this.contentType = s;
    }

    public void setBufferSize(int i) {
        throw new IllegalStateException("Setting buffer size is not supported");
    }

    public int getBufferSize() {
        return 1 << 14;
    }

    public void flushBuffer() throws IOException {
    }

    public void resetBuffer() {
        printWriter = null;
        outputStream = new SimpleServletOutputStream();
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
        resetBuffer();
    }

    public void setLocale(Locale locale) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getLocale() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
