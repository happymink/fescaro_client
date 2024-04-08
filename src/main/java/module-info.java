module com.client.fescaro_client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.client.fescaro_client to javafx.fxml;
    exports com.client.fescaro_client;
}