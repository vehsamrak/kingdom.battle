package battle;

import battle.Handler.ShutdownHandler;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author Vehsamrak
 */
public class Launcher
{

    public void run() throws Exception
    {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8000), 0);

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler(server), "shutdown-thread"));

        HttpContext context = server.createContext("/test", new EchoHandler());
        context.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();
    }

    static class EchoHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

            Headers headers = exchange.getRequestHeaders();
            for (String header : headers.keySet()) {
                stringBuilder.append("<p>").append(header).append("=")
                       .append(headers.getFirst(header)).append("</p>");
            }

            byte[] bytes = stringBuilder.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class Auth extends Authenticator
    {
        @Override
        public Result authenticate(HttpExchange httpExchange)
        {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString())) {
                return new Failure(403);
            } else {
                return new Success(new HttpPrincipal("c0nst", "realm"));
            }
        }
    }
}
