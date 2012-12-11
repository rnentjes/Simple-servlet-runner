package nl.astraeus.http.async;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;

/**
 * User: rnentjes
 * Date: 4/3/12
 * Time: 9:21 PM
 */
public class SimpleHttpRequest extends AttributeParameterHolder implements HttpServletRequest {
    final static String SESSION_COOKIE = "SWSSessionID";

    ByteArrayOutputStream buffer = new ByteArrayOutputStream(2 << 13);

    public SimpleHttpRequest() {
    }



    @Override
    public String getAuthType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getDateHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHeader(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration getHeaders(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration getHeaderNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getIntHeader(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMethod() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPathInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPathTranslated() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getContextPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getQueryString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRemoteUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Principal getUserPrincipal() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRequestedSessionId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRequestURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServletPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpSession getSession() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCharacterEncoding() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getContentLength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getContentType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getProtocol() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getScheme() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServerName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getServerPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRemoteAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRemoteHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Locale getLocale() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration getLocales() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSecure() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRealPath(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRemotePort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLocalPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
