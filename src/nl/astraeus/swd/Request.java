package nl.astraeus.swd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: rnentjes
 * Date: 6/14/12
 * Time: 8:03 PM
 */
public class Request {
    private final static ThreadLocal<Request> current = new ThreadLocal<Request>();

    private HttpServletRequest request;
    private HttpServletResponse response;

    public void setCurrent(HttpServletRequest request, HttpServletResponse response) {
        current.get().request = request;
        current.get().response = response;
    }
}
