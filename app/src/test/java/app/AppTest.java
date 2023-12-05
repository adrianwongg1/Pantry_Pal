/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package app;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import app.Mock.ShareLinkMock;
import app.client.App;
import app.client.View;
import app.client.Controller;
import app.client.Model;
import app.server.ChatGPTHandler;
import app.server.ServerChecker;
import app.server.ShareHandler;
import app.server.MyServer;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.net.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;


class AppTest {
    // Tests whether the prompt we give chatgpt maintains the same provided ingredients as the original recipe
    
    private final String MONGOURI =  "mongodb+srv://PeterNguyen4:Pn11222003-@cluster0.webebwr.mongodb.net/?retryWrites=true&w=majority";

    @Test 
    void testGptSameIngredients() throws IOException {
        MyServer.main(null);
        String mealType = "dinner";
        String ingredients = "steak, potatoes, butter";
        Model model = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = model.performRequest("POST", null, null, prompt, null, "mockGPT");

        // API call should have successfully been made and returned thorugh model with the mealType and ingredients
        assertFalse(response.equals(""));
        MyServer.stop();
    }

    @Test
    void testGptBddRefresh() throws IOException {
        MyServer.main(null);
        // BDD TEST
        String user = "userBDD"; 

        // Scenario: I don't like the recipe generated
        String generatedText = "Scrambled eggs with bacon and toast, Step 1:... Step 2:...";
        // Given: I have chosen breakfast and listed bacon, eggs, and sausage
        // When: I am given a recipe for scrambled eggs with bacon and toast
        // And: I do not want this recipe
        String mealType = "breakfast";
        String ingredients = "bacon, eggs, sausage";
        // Then: when I press the refresh button it will generate another recipe like a bacon egg sandwich
        Model refreshTest = new Model();
        String prompt = "Make me a " + mealType + " recipe using " + ingredients + " presented in JSON format with the \"title\" as the first key with its value as one string, \"ingredients\" as another key with its value as one string, and \"instructions\" as the last key with its value as one string";
        String response = refreshTest.performRequest("POST", user, null, prompt, null, "mockGPT");
        assertNotEquals(response, generatedText);
        MyServer.stop();
    }

    // Tests successful sign up
    @Test
    void testValidSignup() throws IOException {
        MyServer.main(null);
        Model model = new Model();
        String newUser = Long.toHexString(System.currentTimeMillis());
        String password = Long.toHexString(System.currentTimeMillis() + 3);
        String response = model.performRequest("POST", newUser, password, null, null, "signup");
        assertTrue(response.equals("NEW USER CREATED"));
        MyServer.stop();
    }

    // Tests signing up on a name thats taken already 
    @Test
    void testSignupUsernameTaken() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "signup");
        assertEquals("USERNAME TAKEN", response);
        MyServer.stop();
    }

    // Tests a valid login
    @Test
    void testValidLoginValid() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "Bob", "password12", null, null, "login");
        assertEquals("SUCCESS", response);
        MyServer.stop();
    }

    // Tests a invalid login password
    @Test
    void testInvalidLoginCredentials() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "Bob", "wrongPassword", null, null, "login");
        assertEquals("INCORRECT CREDENTIALS", response);
        MyServer.stop();
    }

    // Tests a username that doesn't exist for login
    @Test
    void testLoginDoesntExist() throws IOException { 
        MyServer.main(null);
        Model loginTest = new Model();
        String response = loginTest.performRequest("POST", "fakeName", "password12", null, null, "login");
        assertEquals("USER NOT FOUND", response);
        MyServer.stop();
    }

    // Test /mealtype route to filter breakfast recipes belonging to "testGetMealType" account
    @Test
    void dalleLinkGenerationTest() throws IOException{
        MyServer.main(null);
        Model dalleTest =  new Model();
        String recipeTitle = "Bacon Eggs and Ham";

        String url = "https://www.google.com/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Ff%2Ffa%2FHam_and_eggs_over_easy.jpg%2F1200px-Ham_and_eggs_over_easy.jpg&tbnid=jL-bcwE1AkYVvM&vet=12ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW..i&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FHam_and_eggs&docid=2WM6ZYnDhyPs5M&w=1200&h=789&q=bacon%20eggs%20and%20ham&ved=2ahUKEwjm75GvxvSCAxWwJEQIHRB_BbYQMygBegQIARBW";

        String response = dalleTest.performRequest("POST", null, null, recipeTitle, null, "mockDalle");
        
        assertEquals(url, response);
        MyServer.stop();

    }

    @Test
    void testGetMealType() throws IOException {
        MyServer.main(null);
        String user = "testGetMealType";
        Model mealtype = new Model();
        String response = mealtype.performRequest("GET", user, null, null, "breakfast", "mealtype");
        // Account with username "testGetMealType" has ONE breakfast recipe named "Egg Bacon and Ham Breakfast Recipe"
        assertEquals("Egg Bacon and Ham Breakfast Recipe+breakfast", response);
        MyServer.stop();
    }

    // Test /mealtype route to filter lunch recipes that have not been saved
    @Test
    void testGetNoLunchRecipe() throws IOException {
        MyServer.main(null);
        String user = "testGetMealType";
        Model mealtype = new Model();
        String response = mealtype.performRequest("GET", user, null, null, "lunch", "mealtype");
        // Account with username "testGetMealType" has NO lunch recipes
        assertEquals(null, response);
        MyServer.stop();
    }

    // Test /mealtype route to filter the two dinner recipes belonging to "testGetMealType" account
    @Test
    void testGetMultipleDinnerRecipes() throws IOException {
        MyServer.main(null);
        String user = "testGetMealType";
        Model mealtype = new Model();
        String response = mealtype.performRequest("GET", user, null, null, "dinner", "mealtype");
        // Account with username "testGetMealType" has TWO dinner recipes
        assertEquals("Cheesy Vegetable Tortellini Bake+dinner+Savory Stuffed Pancakes+dinner", response);
        MyServer.stop();
    }

    @Test
    void testServerNotRunning() throws IOException{
        boolean status = ServerChecker.isServerRunning("localhost", 8100);
        assertEquals(false, status);
    }
    
    @Test
    void testServerRunning() throws IOException{
        MyServer.main(null);
        boolean status = ServerChecker.isServerRunning("localhost", 8100);
        assertEquals(true, status);
        MyServer.stop();
    }

    // UNIT TEST
    @Test
    void testGetShareLink() throws IOException{
        // given user has a recipe already
        Mock m = new Mock();
        ShareLinkMock mock = m.new ShareLinkMock("Bryan", "steak and eggs");
        // want to test the share functionality as a unit test
        String web = mock.getWebString();
        assertNotEquals("", web);
        assertTrue(web.contains("Bryan"));
        assertTrue(web.contains("steak and eggs"));
    }

    // Integration Test with model and server
    @Test 
    void shareIntegrationTest() throws IOException{
        MyServer.main(null);
        Model shareTest =  new Model();
        // have a recipe in the database already
        String recipeTitle = "Steak and Egg Skillet";
        String user = "Bryan";
        String error = "The recipe you have selected cannont be found by the server";
        String response = shareTest.performRequest("GET", user, null, null, recipeTitle, "share");

        assertTrue(response.contains(recipeTitle));
        assertFalse(response.contains(error));
        
        MyServer.stop();
    }

    // just testing server request handler method,  GET METHOD
    // USER+TITLE+INGREDIENTS+INSTRUCTIONS+MEALTYPE
    // UNIT TEST
    @Test
    void GETrequestHandlerUnitTest() throws IOException, URISyntaxException{
        MyServer.main(null);
        // have a recipe in the database already
        String recipeTitle = "Hash Brown and Bacon Breakfast Bake";
        String user = "Bryan";
        String ingred = "1 package (20 ounces) refrigerated shredded hash brown potatoes, 8 strips bacon, cooked and crumbled, 2 cups shredded cheddar cheese, 1/2 cup chopped onion, 1/2 cup sour cream, 1/4 teaspoon salt, 1/4 teaspoon pepper and 2 tablespoons butter";
        String instructions = "Preheat oven to 375 degrees F. Grease 9-inch deep dish pie plate. Combine hash browns, bacon, cheese and onion in large bowl. Blend sour cream, salt and pepper; stir into hash brown mixture. Spread in pie plate. Dot with butter. Bake 40 to 45 minutes or until golden brown and bubbly. This is an edit to the recipe";
        String mealtype = "breakfast";
        String method = "GET";
        String query = URLEncoder.encode("u=" + user + "&q=" + recipeTitle, "UTF-8");
        String urlString = "http://localhost:8100/?" + query;
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();    
        in.close();



        assertNotEquals("", response);;
        assertTrue(response.contains(recipeTitle));
        assertTrue(response.contains(ingred));
        assertTrue(response.contains(instructions));

        
        MyServer.stop();
    }


    /**
     * UNIT TEST
     * Test for just the server handler method to post the corret data
     * 
     * removes added data at the end to make sure to not change user recipes
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void POSTrequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);
        // have a recipe in the database already
        String recipeTitle = "pancakes with maple syrup";
        String user = "Bryan";
        String ingred = "flour,eggs,sugar,milk";
        String instructions = "mix ingredients to make batter and then pour into hot pan";
        String mealtype = "breakfast";
        String method = "POST";

        //String query = URLEncoder.encode("u=" + user + "&q=" + recipeTitle, "UTF-8");
        String urlString = "http://localhost:8100/";
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);


        // writing to the body of the request
        String reqBody = user + "+" + recipeTitle + "+" + ingred + "+" + instructions + "+" + mealtype;
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(URLEncoder.encode(reqBody, "UTF-8"));
        out.flush();
        out.close();


        // reading the input
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();    
        in.close();

        assertNotEquals("invalid post", response);

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
      
            Bson filter = eq("title", recipeTitle);
            Bson filter2 = eq("user",user);
            filter = combine(filter,filter2);

            // checkign that post method correctly added to database
            Document recipe = collection.find(filter).first();
            assertEquals(recipeTitle, recipe.getString("title"));
            assertEquals(ingred, recipe.getString("ingredients"));
            assertEquals(instructions,recipe.getString("instructions"));
            assertEquals(user,recipe.getString("user"));
            assertEquals(mealtype, recipe.getString("mealtype"));


            // removing newly added recipe 
            collection.findOneAndDelete(filter);
            recipe = collection.find(filter).first();
            assertNull(recipe);
        }
        
        MyServer.stop();
    }


    
    @Test
    void PUTrequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);
        // have a recipe in the database already channging the ingredients and the instructions
        String recipeTitle = "pancakes";
        String user = "Bryan";
        int random = (int)(Math.random() * 100);
        String ingred = "flour,eggs,sugar,milk," + random + "bacons(number of bacon is random)";
        String instructions = "mix ingredients to make batter and then pour into hot pan with lots of bacon";
        String mealtype = "breakfast";
        String method = "PUT";

        String urlString = "http://localhost:8100/";
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);


        // writing to the body of the request
        String reqBody = user + "+" + recipeTitle + "+" + ingred + "+" + instructions;
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(URLEncoder.encode(reqBody, "UTF-8"));
        out.flush();
        out.close();


        // reading the input
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();    
        in.close();

        assertEquals("valid put", response);

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
      
            Bson filter = eq("title", recipeTitle);
            Bson filter2 = eq("user",user);
            filter = combine(filter,filter2);

            // checkign that post method correctly added to database
            Document recipe = collection.find(filter).first();
            assertEquals(recipeTitle, recipe.getString("title"));
            assertEquals(ingred, recipe.getString("ingredients"));
            assertEquals(instructions,recipe.getString("instructions"));
            assertEquals(user,recipe.getString("user"));
            assertEquals(mealtype, recipe.getString("mealtype"));

        }
        
        MyServer.stop();
    }

    @Test
    void DELETErequestHandlerTest() throws IOException, URISyntaxException{
        MyServer.main(null);

        // setting up a fake recipe to test the DELETE endpoint for requesthandler route
        String t = "testTitle";
        String i = "testIngredients";
        String ins = "testInstructinos";
        String u = "testUser";
        String m = "testMealtype";

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");
            
            Document recipe = new Document("_id", new ObjectId());
            recipe.append("title", t);
            recipe.append("ingredients", i);
            recipe.append("instructions", ins);
            recipe.append("user", u);
            recipe.append("mealtype", m);

            collection.insertOne(recipe);
        }

        // starting the delete request
        String method = "DELETE";
        String query = URLEncoder.encode("u=" + u + "&q=" + t, "UTF-8");
        String urlString = "http://localhost:8100/?" + query;
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);

        // reading the output
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();  
        assertEquals("valid delete", response);
        
        in.close();

        try (MongoClient mongoClient = MongoClients.create(MONGOURI)) {
            MongoDatabase database = mongoClient.getDatabase("PantryPal");
            MongoCollection<Document> collection = database.getCollection("recipes");

            Bson filter = Filters.and(Filters.eq("title",t),Filters.eq("user", u));
            Document recipe = collection.find(filter).first();
            assertNull(recipe);
        }

        
        MyServer.stop();
    }


}