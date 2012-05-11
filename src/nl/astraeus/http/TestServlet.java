package nl.astraeus.http;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 10:40 AM
 */
public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Posted!");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("<html><body>");

        resp.getWriter().println("<form id=\"fileupload\" action=\"\" method=\"POST\" enctype=\"multipart/form-data\">");

        resp.getWriter().println("<input type=\"file\" name=\"files[]\" multiple />");
        resp.getWriter().println("<input type=\"submit\" name=\"submit\" value=\"submit\" />");

        resp.getWriter().println("</form>");

        resp.getWriter().println("</body></html>");

        resp.addCookie(new Cookie("Rien", "Is Pipo!"));
    }

    public static void main(String [] args) {
        SimpleWebServer server = new SimpleWebServer(8080);

        server.addServlet(new TestServlet(), "/*");

        server.start();

    }
}
