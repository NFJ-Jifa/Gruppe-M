module com.gruppem.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;


    opens com.gruppem.energygui to javafx.fxml;
    exports com.gruppem.energygui;
}