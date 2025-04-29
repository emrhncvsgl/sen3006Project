package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the Task Management application.
 * Demonstrates both Composite and Observer design patterns.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Task Management Application");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
