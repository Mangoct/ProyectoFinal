module com.example.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires itextpdf;
    requires java.sql;
    requires java.mail;
    requires activation;


    opens com.example.proyectofinal to javafx.fxml;
    exports com.example.proyectofinal;
}