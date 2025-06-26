/**
 * Module declaration for the Energy GUI application.
 * Specifies dependencies and module visibility for JavaFX and HTTP communication.
 */
module com.gruppem.energygui {

    // Required for UI components like Button, Label, TableView, etc.
    requires javafx.controls;

    // Required to load and use FXML-based layouts
    requires javafx.fxml;

    // Required for making HTTP requests to the REST API (Java 11+)
    requires java.net.http;

    // Required for parsing JSON responses (e.g. org.json.JSONObject)
    requires org.json;

    // Makes the main package available to other modules (e.g., launcher)
    exports com.gruppem.energygui;

    // Allows JavaFX to access controller classes via reflection (needed for FXML)
    opens com.gruppem.energygui to javafx.fxml;
}
