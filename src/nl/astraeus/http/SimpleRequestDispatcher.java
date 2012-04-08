package nl.astraeus.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * User: rnentjes
 * Date: 4/8/12
 * Time: 8:28 PM
 */
public class SimpleRequestDispatcher implements RequestDispatcher {

    private HttpServlet servlet;

    public SimpleRequestDispatcher(HttpServlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        SimpleHttpRequest req = (SimpleHttpRequest)servletRequest;
        SimpleHttpResponse res = (SimpleHttpResponse)servletResponse;

        res.resetBuffer();

        servlet.service(req, res);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        SimpleHttpRequest req = (SimpleHttpRequest)servletRequest;
        SimpleHttpResponse res = (SimpleHttpResponse)servletResponse;

        servlet.service(req, res);
    }

}
