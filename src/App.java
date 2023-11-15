import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;
import javafx.scene.text.*;
import java.io.*;
import javax.sound.sampled.*;

class Recipe extends VBox {

    private Label index;
    private TextField recipe;
    private Button viewButton;
    private Stage primaryStage;
    
    Recipe(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.setPrefSize(500, 20); // sets size of recipe
        this.setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0; -fx-font-weight: bold;"); // sets background color of recipe
        
        index = new Label();
        index.setText(""); // create index label
        index.setPrefSize(40, 20); // set size of Index label
        index.setTextAlignment(TextAlignment.CENTER); // Set alignment of index label
        index.setPadding(new Insets(10)); // adds some padding to the recipe
        this.getChildren().add(index); // add index label to recipe

        recipe = new TextField(); // create recipe name text field
        recipe.setPrefSize(200, 20); // set size of text field
        recipe.setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0;"); // set background color of texfield
        index.setTextAlignment(TextAlignment.LEFT); // set alignment of text field
        recipe.setPadding(new Insets(10, 0, 10, 0)); // adds some padding to the text field
        this.getChildren().add(recipe); // add textlabel to recipe

        viewButton = new Button("View");
        viewButton.setPrefSize(250, 20);
        viewButton.setPrefHeight(Double.MAX_VALUE);
        viewButton.setStyle("-fx-background-color: #FAE5EA; -fx-border-width: 0;"); // sets style of button
        this.getChildren().add(viewButton);

        addListeners();
    }

    public void setRecipeIndex(int num) {
        this.index.setText(num + ""); // num to String
        this.recipe.setPromptText("Recipe ");
    }

    public TextField getRecipe() {
        return this.recipe;
    }

    public Button getViewButton(){
        return this.viewButton;
    }

    public void addListeners(){
        viewButton.setOnAction(e1 -> {

            Scene initial = primaryStage.getScene();
            RecipePane recipeScreen = new RecipePane();
            Button backButton = recipeScreen.getFooter().getBackButton();
            backButton.setOnAction(e2 -> {
                primaryStage.setScene(initial);
            });
            primaryStage.setTitle("Recipe");
            primaryStage.setScene(new Scene(recipeScreen, 400, 500));

        });
    }

}

// Container to hold recipes on main page
class RecipeList extends VBox {
    RecipeList() {
        this.setSpacing(5); // sets spacing between recipe
        this.setPrefSize(500, 560);
        this.setStyle("-fx-background-color: #F0F8FF;");
    }

    public void updateRecipeIndices() {
        int index = 1;
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i) instanceof Recipe) {
                ((Recipe) this.getChildren().get(i)).setRecipeIndex(index);
                index++;
            }
        }
    }

    public void loadTasks(int index){
        try {
            BufferedReader in = new BufferedReader(new FileReader("recipe.csv"));
            String line = in.readLine();
            String combine = "";
            while (line != null) {
                if (combine.equals("")) {
                    combine = combine + line;
                } else {
                    combine = combine + "\n" + line;
                }
                line = in.readLine();
            }
            String[] recipes = combine.split("\\$");
            RecipeSteps current = new RecipeSteps();
            current.getTextArea().setText(recipes[index-1]);
            this.getChildren().add(current);
            in.close();
        } catch(Exception e){
            System.out.println("LOAD FAIL");
        }
    }
}

// Footer for main page
class Footer extends HBox {

    private Button newRecipeButton;

    Footer() {
        this.setPrefSize(500, 60);
        this.setStyle("-fx-background-color: #F0F8FF;");
        this.setSpacing(15);

        // set a default style for buttons - background color, font size, italics
        String defaultButtonStyle = "-fx-font-style: italic; -fx-background-color: #FFFFFF;  -fx-font-weight: bold; -fx-font: 12 monaco;";

        newRecipeButton = new Button("New Recipe"); // text displayed on add button
        newRecipeButton.setStyle(defaultButtonStyle); // styling the button
        
        this.getChildren().add(newRecipeButton); // adding button to footer
        this.setAlignment(Pos.CENTER); // aligning the buttons to center
    }

    public Button getNewRecipeButton() {
        return newRecipeButton;
    }
}

// App Header for all pages
class Header extends HBox {

    Header() {
        this.setPrefSize(500, 70); // Size of the header
        this.setStyle("-fx-background-color: #39A7FF;");

        Text titleText = new Text("PantryPal"); // Text of the Header
        titleText.setStyle("-fx-font: 24 arial; -fx-text-fill: #FFFFFF;");
        this.getChildren().add(titleText);
        this.setAlignment(Pos.CENTER); // Align the text to the Center
    }

}

class AppFrame extends BorderPane {

    private Header header;
    private Footer footer;
    public RecipeList recipeList;
    private Button newRecipeButton;

    private Stage primaryStage;
    public Scene homeScene;

    AppFrame() {
        // Initialize the header Object
        header = new Header();

        // Create a recipeList Object to hold the recipes
        recipeList = new RecipeList();
        
        // Initialize the Footer Object
        footer = new Footer();

        // Add a Scroller to the recipe List
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(recipeList);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);

        // Add header to the top of the BorderPane
        this.setTop(header);
        // Add scroller to the centre of the BorderPane
        this.setCenter(scroll);
        // Add footer to the bottom of the BorderPane
        this.setBottom(footer);

        // Initialise Button Variables through the getters in Footer
        newRecipeButton = footer.getNewRecipeButton();
        // Call Event Listeners for the Buttons
        addListeners();
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void switchScene(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
    } 

    public void addListeners()
    {
        // New recipe button event
        newRecipeButton.setOnAction(e -> {
            MealFrame mealType = new MealFrame(this.primaryStage, this.primaryStage.getScene(), recipeList);
            Scene recordMeal = new Scene(mealType, 400, 500);
            primaryStage.setScene(recordMeal);
        });        
    }
}

class RecipePane extends BorderPane {
    private FooterTwo footer;
    private HeaderTwo header;
    private RecipeSteps recipeSteps;

    public FooterTwo getFooter(){
        return this.footer;
    }

    // private Button saveButton;
    // private Button deleteButton;
    private Button backButton;

    private Stage primaryStage;

    public Scene homeScene;

    RecipePane() {
        header = new HeaderTwo();
        
        recipeSteps = new RecipeSteps();

        footer = new FooterTwo();

        // saveButton = footer.getSaveButton();
        // deleteButton = footer.getDeleteButton();

        recipeSteps.getTextArea().setText("Hello");
        backButton = footer.getBackButton();

        ScrollPane s = new ScrollPane(recipeSteps);
        this.setRight(s);
        s.setFitToHeight(true);
        s.setFitToWidth(true);

        // Add header to the top of the BorderPane
        this.setTop(header);
        // Add scroller to the centre of the BorderPane
        this.setCenter(recipeSteps);
        // Add footer to the bottom of the BorderPane
        this.setBottom(footer);

        addListeners();
    }

    public void addListeners(){

        backButton.setOnAction(e ->{
            primaryStage.setScene(homeScene);
        });

    }
}

class RecipeSteps extends HBox {
    public TextArea recipeSteps;

    RecipeSteps() {
        recipeSteps = new TextArea();
        recipeSteps.setEditable(true);
        recipeSteps.setPrefSize(700, 700); // set size of text field
        recipeSteps.setStyle("-fx-background-color: #DAE5EA; -fx-border-width: 0;"); // set background color of texfield
        
        this.getChildren().add(recipeSteps);

    }

    public TextArea getTextArea(){
        return this.recipeSteps;
    }

}

class HeaderTwo extends HBox {

    HeaderTwo() {
        this.setPrefSize(500, 60); // Size of the header
        this.setStyle("-fx-background-color: #F0F8FF;");

        Text titleText = new Text("Recipe"); // Text of the Header
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 20;");
        this.getChildren().add(titleText);
        this.setAlignment(Pos.CENTER); // Align the text to the Center
    }
}

class FooterTwo extends HBox {

    private Button backButton;
    private Button saveButton;
    private Button deleteButton;

    FooterTwo() {
        this.setPrefSize(500, 60);
        this.setStyle("-fx-background-color: #F0F8FF;");
        this.setSpacing(15);

        // set a default style for buttons - background color, font size, italics
        String defaultButtonStyle = "-fx-font-style: italic; -fx-background-color: #FFFFFF;  -fx-font-weight: bold; -fx-font: 11 arial;";

        backButton = new Button("Go Back");
        backButton.setStyle(defaultButtonStyle);
        saveButton = new Button("Save");
        saveButton.setStyle(defaultButtonStyle);
        deleteButton = new Button("Delete");
        deleteButton.setStyle(defaultButtonStyle);
        

        this.getChildren().addAll(backButton, saveButton, deleteButton); // adding buttons to footer
        this.setAlignment(Pos.CENTER); // aligning the buttons to center

    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

}

    
public class App extends Application {
    Scene homeScene;
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Set the title of the a
        primaryStage.setTitle("PantryPal");

        // Setting the Layout of the Window- Should contain a Header, Footer and the recipeList
        AppFrame home = new AppFrame();
        home.setStage(primaryStage);

        // Set up Home Page and Record Recipe pages
        homeScene = new Scene(home, 400, 500);

        // Create scene of mentioned size/ with the border pane
        primaryStage.setScene(homeScene);

        // Make window non-resizable
        primaryStage.setResizable(false);
        // Show the app
        primaryStage.show();

        // String css = this.getClass().getResource("style.css").toExternalForm();
        // homeScene.getStylesheets().add(css);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
