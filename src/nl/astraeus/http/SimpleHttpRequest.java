package nl.astraeus.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.util.*;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 9:21 PM
 */
public class SimpleHttpRequest extends AttributeParameterHolder implements HttpServletRequest {
    final static String SESSION_COOKIE = "SWSSessionID";

    private String queryString;
    private HttpMethod httpMethod = null;
    private String uri;
    private boolean http11;
    private boolean headersRead;
    private boolean keepAlive;
    private boolean multiPartFormData;
    private SimpleHttpSession session;
    private SimpleWebServer server;
    private String contentType;
    private int contentLength = -1;
    private Cookie [] cookies;


    public SimpleHttpRequest(SimpleWebServer server, HttpMethod httpMethod, String requestString, boolean http11) {
        this.server = server;
        this.session = null;
        this.httpMethod = httpMethod;
        this.http11 = http11;
        this.headersRead = false;
        this.keepAlive = http11;
        this.contentType = "";
        this.cookies = new Cookie[0];

        int qmloc = requestString.indexOf('?');

        if (qmloc > -1) {
            uri = requestString.substring(0, qmloc);
            queryString = requestString.substring(qmloc+1);
        } else {
            uri = requestString.trim();
            queryString = "";
        }
    }

    boolean headersRead() {
        return headersRead;
    }

    boolean isMultiPartFormData() {
        return multiPartFormData;
    }

    void readHeaders(Map<HttpHeader, String> headers) throws IOException {
        headersRead = true;

        keepAlive = headers.get(HttpHeader.CONNECTION) != null;
        if (headers.get(HttpHeader.CONTENT_LENGTH) != null) {
            contentLength = Integer.parseInt(headers.get(HttpHeader.CONTENT_LENGTH));
        } else {
            contentLength = 0;
        }
        parseCookies(headers.get(HttpHeader.COOKIE));
        contentType = headers.get(HttpHeader.CONTENT_TYPE);

        multiPartFormData = contentType != null && contentType.contains("multipart/form-data;");
    }

    void parseRequestParameters(String formdata) throws UnsupportedEncodingException {

        if (formdata != null && !formdata.isEmpty()) {
            if (formdata.endsWith("&")) {
                formdata = formdata + queryString;
            } else {
                formdata = formdata + "&" + queryString;
            }

            String [] parts = formdata.split("\\&");

            for (String part : parts) {
                String [] sp = part.split("=");

                if (sp.length == 1) {
                    String name = URLDecoder.decode(sp[0],"UTF-8");
                    String value = "";

                    addParameter(name, value);
                } else if ((sp.length == 2)) {
                    String name = URLDecoder.decode(sp[0],"UTF-8");
                    String value = URLDecoder.decode(sp[1],"UTF-8");

                    addParameter(name, value);
                }
            }
        }
    }

    private void parseCookies(String in) {
        Set<Cookie> cookies = new HashSet<Cookie>();
        if (in != null) {
            String [] parts = in.split("\\;");

            for (String part : parts) {
                String [] sp = part.trim().split("\\=");

                if (sp.length == 2) {
                    if (sp[0].trim().equals(SESSION_COOKIE)) {
                        session = server.getSession(sp[1]);
                    } else {
                        cookies.add(new Cookie(sp[0], sp[1]));
                    }
                }
            }
        }
        this.cookies = cookies.toArray(new Cookie[0]);
    }

    HttpMethod getHttpMethod() {
        return httpMethod;
    }

    void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return this.cookies;
    }

    public long getDateHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHeader(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaders(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaderNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIntHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getMethod() {
        return httpMethod.toString();
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return "/";
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRemoteUser() {
        return null;
    }

    public boolean isUserInRole(String s) {
        return false;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getRequestURI() {
        return uri;
    }

    public StringBuffer getRequestURL() {
        return null;
    }

    public String getServletPath() {
        return uri;
    }

    public HttpSession getSession(boolean b) {
        HttpSession result = session;

        if (b && session == null) {
            result = getSession();
        }

        return result;
    }

    public HttpSession getSession() {
        if (session == null) {
            session = server.getSession(null);
        }

        session.setLastAccessedTime();

        return session;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public String getCharacterEncoding() {
        return "UTF-8";
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new IllegalStateException("Setting of character encoding not supported, it's hardcoded to UTF-8");
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public String getProtocol() {
        return "http";
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return "Simple-web-server";
    }

    public int getServerPort() {
        return server.getPort();
    }

    public BufferedReader getReader() throws IOException {
        throw new IllegalStateException("Not supported");
    }

    public String getRemoteAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getLocale() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getLocales() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSecure() {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return new SimpleRequestDispatcher(server.findHandlingServlet(uri));
    }

    public String getRealPath(String s) {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getLocalName() {
        return "";
    }

    public String getLocalAddr() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getLocalPort() {
        return 0;
    }

    public boolean getHttp11() {
        return http11;
    }

    public boolean getKeepAlive() {
        return keepAlive;
    }

    //@CheckForNull
    public String getSessionId() {
        String result = null;

        if (getSession(false) != null) {
            result = getSession().getId();
        }

        return result;
    }
}
