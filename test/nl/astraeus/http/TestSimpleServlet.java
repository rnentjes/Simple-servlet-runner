package nl.astraeus.http;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
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

    private void postToURL(String u, String data) throws IOException {
        URL url = new URL(u);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" +
                Integer.toString(data.getBytes().length));
        connection.setDoOutput(true);
        OutputStreamWriter out = null;

        try {
            out = new OutputStreamWriter(connection.getOutputStream());

            out.write(data);
        } finally {
            if (out != null) {
                out.close();
            }
        }


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
    }

    public static class HelloWorldServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if ("true".equals(req.getSession().getAttribute("switch"))) {
                resp.getWriter().println("Goodbye world!");
            } else {
                resp.getWriter().println("Hello world!");
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.getSession().setAttribute("switch", "true".equals(req.getParameter("switch")));
        }
    }

    @Test
    public void testSimpleServlet() throws IOException {
        SimpleWebServer server = new SimpleWebServer(9999);

        server.addServlet(new HelloWorldServlet(), "/test");

        server.start();

        String result = readURL("http://127.0.0.1:9999/test");

        server.stop();

        Assert.assertEquals("Hello world!\n", result);
    }

    @Ignore
    @Test
    public void testSimpleServletPost() throws IOException {
        SimpleWebServer server = new SimpleWebServer(9876);

        server.addServlet(new HelloWorldServlet(), "/test");

        server.start();

        String result = readURL("http://localhost:9876/test");

        postToURL("http://localhost:9876/test", "switch=true&");

        String result2 = readURL("http://localhost:9876/test");

        server.stop();

        Assert.assertEquals("Hello world!\n", result);
        Assert.assertEquals("Goodbye world!\n", result2);
    }

}
