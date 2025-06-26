package com.gruppem.energygui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the JavaFX application.
 * This class initializes and displays the energy dashboard UI.
 */
public class MainApplication extends Application {

    /**
     * Called by the JavaFX runtime to start the application.
     * Loads the FXML layout and shows the primary stage (window).
     *
     * @param stage the main window of the application
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Load the layout from the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gruppem/energygui/MainView.fxml"));
        Parent root = loader.load();

        // Create and display the main scene
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Energy Community Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX application.
     * This method is the real entry point of the program.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}
