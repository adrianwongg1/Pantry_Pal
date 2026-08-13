package app.server;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/**
 *  implementing a handle api route so that we can send audio files to server to transcribe, also call chatgpt with strings to retrun a prompt.
 * 
 *  This is the handler for the third party api calls needed to chat gpt.
 * 
 *  methods needed: 
 *  post -> give it prompt and gives back the generate recipe 
 *  
 */
public class ChatGPTHandler implements HttpHandler {
    private static final String API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

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
            response = "Recipe generation failed: " + e.getMessage();
        }
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = httpExchange.getResponseBody()) { output.write(bytes); }
        
    }

    // need to feed this method the entire prompt before calling
    // prompt: Make me a breakfast recipe using eggs with the recipe in json format with ingredients as one string and instructions as one string 
    // returns title+ingredients+instructions

    private String handlePost(HttpExchange httpExchange) throws IOException, InterruptedException{
        String generatedText = "no response from chatgptHandler";
        // get the prompt from the body
        InputStream inStream = httpExchange.getRequestBody();
        Scanner scanner = new Scanner(inStream);
        
        String prompt = URLDecoder.decode(scanner.nextLine(), "UTF-8"); 
        
        int tokens = 700;
        // Create a request body which you will pass into request object
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", Configuration.textModel());
        requestBody.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content",
                "Create a practical recipe. Return exactly these three labels on separate lines: " +
                "Title:, Ingredients:, Instructions:. Keep each value on one line."))
            .put(new JSONObject().put("role", "user").put("content", prompt)));
        requestBody.put("max_tokens", tokens);
        requestBody.put("temperature", 1.0);
        // Create the HTTP Client=
        HttpClient client = HttpClient.newHttpClient();
        // Create the request object
        HttpRequest request = HttpRequest
        .newBuilder()
        .uri(URI.create(API_ENDPOINT))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + Configuration.groqApiKey())
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
            throw new IOException("Groq returned HTTP " + response.statusCode() + ": " + responseBody);
        }
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray choices = responseJson.getJSONArray("choices");
        generatedText = choices.getJSONObject(0).getJSONObject("message").getString("content");

        // parse generated text
        int titleStart = generatedText.indexOf("Title:");
        int ingStart = generatedText.indexOf("Ingredients:");
        int insStart = generatedText.indexOf("Instructions:");
        String titleString = "Title:";
        String ingString = "Ingredients:";
        String insString = "Instructions:";
        int skipTitle = titleString.length();
        int skipIng = ingString.length();
        int skipIns = insString.length();

        if (titleStart < 0 || ingStart < 0 || insStart < 0) {
            throw new IOException("Groq returned a recipe in an unexpected format.");
        }
        String title = generatedText.substring(titleStart+skipTitle,ingStart);
        String ing = generatedText.substring(ingStart+skipIng,insStart);
        String ins = generatedText.substring(insStart+skipIns);

        // cleaning newlines from output
        title = title.replace("\n", "");
        ing = ing.replace("\n","");
        ins = ins.replace("\n", "");
        // Remove leading and trailing spaces
        title = title.trim();
        ing = ing.trim();
        ins = ins.trim();

        System.out.println("title:" + title);
        System.out.println("ingredients" + ing);
        System.out.println("instructions;" + ins);

        String res = title + "+" + ing + "+" + ins;

        System.out.println("responsebody:" + responseBody);
        System.out.println("return" + res);
        scanner.close();
        res = res.replace("&", " and ");
        return res;
    }
}
