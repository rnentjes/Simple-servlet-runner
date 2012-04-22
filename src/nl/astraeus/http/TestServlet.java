package nl.astraeus.http;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 10:40 AM
 */
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().print("Dit is de test servlet!");

        resp.addCookie(new Cookie("Rien", "Is Pipo!"));
    }

    public static void main(String [] args) {
        SimpleWebServer server = new SimpleWebServer(8080);

        server.addServlet(new TestServlet(), "/*");

        server.start();

    }
}
