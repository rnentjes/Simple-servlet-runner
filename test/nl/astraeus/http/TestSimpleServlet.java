package nl.astraeus.http;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: rnentjes
 * Date: 4/8/12
 * Time: 6:57 PM
 */
public class TestSimpleServlet {

    private static String readInputStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();

        while(reader.ready()) {
            buffer.append(reader.readLine());
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private String readURL(String u) throws IOException {
        URL url = new URL(u);

        URLConnection connection = url.openConnection();
        connection.connect();

        InputStream in = null;
        String result = null;

        try {
            in = connection.getInputStream();

            result = readInputStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
       }

        return result;
    }

    public static class HelloWorldServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().println("Hello world!");
        }
    }

    @Test
    public void testSimpleServlet() throws IOException {
        SimpleWebServer server = new SimpleWebServer(9999);

        server.addServlet(new HelloWorldServlet(), "/test");

        server.start();

        String result = readURL("http://localhost:9999/test");

        server.stop();

        Assert.assertEquals("Hello world!\n", result);
    }

}
