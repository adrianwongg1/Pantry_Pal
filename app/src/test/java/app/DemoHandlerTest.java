package app;

import app.server.DemoHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoHandlerTest {
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        DemoHandler handler = new DemoHandler();
        server.createContext("/", handler);
        server.createContext("/login", handler);
        server.createContext("/chatgpt", handler);
        server.createContext("/dalle", handler);
        server.createContext("/load-recipe", handler);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void demoBackendSuppliesTheMainAppFlowsWithoutExternalServices() throws Exception {
        assertEquals("SUCCESS", request("POST", "/login", "demo&demo"));
        assertTrue(request("POST", "/chatgpt", "recipe request").contains("Fluffy Garden Omelet"));
        assertTrue(request("GET", "/load-recipe?q=demo", null).contains("Tomato Basil Pasta"));
        assertTrue(request("POST", "/dalle", "image request").startsWith("file:"));
    }

    private String request(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path));
        if (body == null) request.GET();
        else request.method(method, HttpRequest.BodyPublishers.ofString(body));
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString()).body();
    }
}
