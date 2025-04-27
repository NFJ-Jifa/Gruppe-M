module com.gruppem.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens com.gruppem.energygui to javafx.fxml;
    exports com.gruppem.energygui;
}