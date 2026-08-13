package app.server;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;


/*
 * This class starts our server on the localhost,
 * must be ran before running our gradle app
 * 
 */

public class MyServer {

    // initialize server port and hostname
    private static final int SERVER_PORT = 8100;
    private static final String SERVER_HOSTNAME = "LOCALHOST";

    public static final String MONGO_URI = Configuration.mongoUri();
    
    private static HttpServer server;
    private static ThreadPoolExecutor threadPoolExecutor;

    public static void main(String[] args) throws IOException {

        // create a thread pool to handle requests
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        // create a server
        server = HttpServer.create(
            new InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT),
            0
        );

        if (Configuration.demoMode()) {
            DemoHandler demoHandler = new DemoHandler();
            server.createContext("/", demoHandler);
            server.createContext("/whisper", demoHandler);
            server.createContext("/chatgpt", demoHandler);
            server.createContext("/login", demoHandler);
            server.createContext("/signup", demoHandler);
            server.createContext("/load-recipe", demoHandler);
            server.createContext("/dalle", demoHandler);
            server.createContext("/mealtype", demoHandler);
            server.createContext("/share", demoHandler);
            server.createContext("/picture", demoHandler);
            System.out.println("PantryPal is running in offline demo mode.");
        } else {
            server.createContext("/", new RequestHandler());
            server.createContext("/whisper", new WhisperHandler());
            server.createContext("/chatgpt", new ChatGPTHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/signup", new SignupHandler());
            server.createContext("/load-recipe", new loadRecipeHandler());
            server.createContext("/dalle", new DallEHandler());
            server.createContext("/mockDalle", new MockDallE());
            server.createContext("/mealtype", new MealTypeFilterHandler());
            server.createContext("/share", new ShareHandler());
            server.createContext("/mockGPT", new MockGPT());
            server.createContext("/mockwhisper", new MockWhisper());
            server.createContext("/picture", new pictureHandler());
        }

        server.setExecutor(threadPoolExecutor);
        server.start();
        
        System.out.println("Server started on port " + SERVER_PORT);
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (threadPoolExecutor != null) {
            // HttpServer.stop() intentionally leaves the executor running, since it
            // doesn't own it. Its threads are non-daemon, so without this the JVM
            // (and the Dock icon) never exits after the window closes.
            threadPoolExecutor.shutdownNow();
            threadPoolExecutor = null;
        }
    }

}
