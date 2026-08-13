package app.server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * implementing the DALL-E Handler that parses a prompt
 * that generates an image based on the prompt and then
 * returns the generated URL
 * 
 * Uses: POST
 * 
 */

public class DallEHandler implements HttpHandler{
    private static final String API_ENDPOINT = "https://api.openai.com/v1/images/generations";
    private Path imagePath;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String response = "Request received";
        int status = 200;
        try {
            if (method.equals("POST")) {
              response = handlePost(httpExchange);
            } else {
              throw new Exception("Not Valid Request Method");
            }

        } catch (Exception e) {
            status = 503;
            response = "Image generation failed: " + e.getMessage();
        }
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = httpExchange.getResponseBody()) { output.write(bytes); }

    }

    /*
     * Handles the prompt given to the httpExchange
     * Spits out the generated image URL from the servers
     * 
     */
    private String handlePost(HttpExchange httpExchange) throws IOException, InterruptedException {
        // Set request parameters
        InputStream inStream = httpExchange.getRequestBody();
        Scanner scanner = new Scanner(inStream);
        
        String prompt = URLDecoder.decode(scanner.nextLine(), java.nio.charset.StandardCharsets.UTF_8);
        
        // Create a request body which you will pass into request object
        JSONObject requestBody = new JSONObject();
    
        requestBody.put("model", Configuration.imageModel());
        requestBody.put("prompt", prompt);
        requestBody.put("size", "1024x1024");

        // Create the HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create the request object
        HttpRequest request = HttpRequest
            .newBuilder()
            .uri(URI.create(API_ENDPOINT))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + Configuration.openAiApiKey())
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();


        // Send the request and receive the response
        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        // Process the response
        String responseBody = response.body();

        if (response.statusCode() / 100 != 2) {
            throw new IOException("OpenAI returned HTTP " + response.statusCode() + ": " + responseBody);
        }
        JSONObject image = new JSONObject(responseBody).getJSONArray("data").getJSONObject(0);

        // GPT Image returns base64 data. Persist it outside source resources and give
        // JavaFX a local file URL; a configured legacy image model may still return a URL.
        if (image.has("b64_json")) {
            Path imageDirectory = Paths.get("generated-images");
            Files.createDirectories(imageDirectory);
            imagePath = imageDirectory.resolve("recipe-" + System.currentTimeMillis() + ".png");
            Files.write(imagePath, Base64.getDecoder().decode(image.getString("b64_json")));
            scanner.close();
            return imagePath.toAbsolutePath().toUri().toString();
        }

        String generatedImageURL = image.getString("url");
        scanner.close();
        return generatedImageURL;
    }

    public Path getImagePath(){
        return imagePath;
    }

}
