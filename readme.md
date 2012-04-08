```java
public static void main(String[] args) {
    SimpleWebServer server = new SimpleWebServer(8080);

    server.addServlet(new HelloWorldServlet(), "/test");

    server.start();
}

public class HelloWorldServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
            
        resp.getWriter().println("Hello world!");
    }
}
```    