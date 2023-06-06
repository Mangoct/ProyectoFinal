package com.example.proyectofinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Main extends Application {

    final String FICHERO_CONFIGURACION = "settings.properties";
    static Properties configuracion = new Properties();

    public static Connection conn;

    @Override
    public void start(Stage stage) throws IOException {

        cargarConfiguracion(FICHERO_CONFIGURACION,configuracion);
        contectarBaseDeDatos(configuracion);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Las piedras controlador animal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    private void cargarConfiguracion(String ficheroConfiguracion, Properties config) {
        try {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(ficheroConfiguracion);
            config.load(input);
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al leer el fichero de configuración");
            alerta.setTitle("Error al leer el fichero de configuración");
            alerta.setContentText("ERROR: No se ha podido leer el contenido del fichero " + ficheroConfiguracion );
            alerta.showAndWait();
            System.exit(1);
        }
    }

    public void contectarBaseDeDatos(Properties config) {
        try {

            String url = config.getProperty("db.url");
            String user = config.getProperty("db.user");
            String password = config.getProperty("db.password");
            String driver = config.getProperty("db.driver");

            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);

        } catch ( Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error no se ha podido conectar a la base de datos");
            alerta.setTitle("Error no se ha podido conectar a la base de datos");
            alerta.setContentText("ERROR: No se ha podido conectar a la base de datos  " + config.getProperty("db.url") );
            alerta.showAndWait();
            System.exit(2);
        }

    }
}