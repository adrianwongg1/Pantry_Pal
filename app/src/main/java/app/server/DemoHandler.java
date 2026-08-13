package app.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Offline backend used for demos. It deliberately uses no network services,
 * credentials, microphone transcription, or persistent user data.
 */
public final class DemoHandler implements HttpHandler {
    private static final String FEATURED_TITLE = "Fluffy Garden Omelet";
    private static final String FEATURED_RECIPE = FEATURED_TITLE
        + "+Eggs, baby spinach, feta, cherry tomatoes, olive oil"
        + "+1. Whisk the eggs. 2. Saute spinach and tomatoes. 3. Add eggs and feta. 4. Fold and serve.";
    private static final String RECIPE_LIST = FEATURED_TITLE + "+breakfast_"
        + "Tomato Basil Pasta+lunch_" + "Sheet Pan Veggie Bowl+dinner";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            response = responseFor(exchange);
        } catch (Exception exception) {
            response = "Demo mode error: " + exception.getMessage();
        }
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String responseFor(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/login".equals(path)) return "SUCCESS";
        if ("/signup".equals(path)) return "NEW USER CREATED";
        if ("/whisper".equals(path)) return "breakfast eggs, spinach, feta, and tomatoes";
        if ("/chatgpt".equals(path) || "/mockGPT".equals(path)) return FEATURED_RECIPE;
        if ("/dalle".equals(path) || "/mockDalle".equals(path)) return sampleImageUrl();
        if ("/load-recipe".equals(path)) return RECIPE_LIST;
        if ("/mealtype".equals(path)) return recipesForMealType(exchange);
        if ("/picture".equals(path)) return sampleImageUrl();
        if ("/share".equals(path)) return demoSharePage();

        // Saving, editing, and deleting are accepted for the current demo session.
        if ("/".equals(path) && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return "valid demo request";
        }
        if ("/".equals(path) && "GET".equals(method)) return FEATURED_RECIPE;
        return "Demo mode supports this screen without external services.";
    }

    private String recipesForMealType(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return RECIPE_LIST;
        Map<String, String> parameters = QueryParser.parseQuery(URLDecoder.decode(query, StandardCharsets.UTF_8));
        String mealType = parameters.get("q");
        if ("breakfast".equals(mealType)) return FEATURED_TITLE + "+breakfast";
        if ("lunch".equals(mealType)) return "Tomato Basil Pasta+lunch";
        if ("dinner".equals(mealType)) return "Sheet Pan Veggie Bowl+dinner";
        return RECIPE_LIST;
    }

    private String sampleImageUrl() {
        return DemoHandler.class.getResource("/image_1701768936700.jpg").toExternalForm();
    }

    private String demoSharePage() {
        return "<html><body><h1>" + FEATURED_TITLE + "</h1><h2>Breakfast</h2><p>"
            + "Eggs, baby spinach, feta, cherry tomatoes, olive oil</p><p>"
            + "Whisk, saute, fold, and serve.</p></body></html>";
    }
}
