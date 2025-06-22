module com.gruppem.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;

    exports com.gruppem.energygui;
    opens com.gruppem.energygui to javafx.fxml;
}
