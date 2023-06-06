package com.example.proyectofinal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Data_controller {

    public Button Btn_volver;
    @FXML
    private Button btn_Añadir;


    @FXML
    private TableColumn<RegistroDiarioDetalle, Integer> col_Bajas;

    @FXML
    private TableColumn<RegistroDiarioDetalle, String> col_Dia;

    @FXML
    private TableColumn<RegistroDiarioDetalle, Integer> col_Entrada;

    @FXML
    private TableColumn<RegistroDiarioDetalle, Integer> col_Salida;

    @FXML
    private TableColumn<RegistroDiarioDetalle, Integer> col_Total;

    @FXML
    public TableView<RegistroDiarioDetalle> tabla_DatosNave;

    private ObservableList<RegistroDiarioDetalle> registros = FXCollections.observableArrayList();
    private Contrato contratoSeleccionado;

    public void inicializarDatosDetalle1(Contrato contratoSeleccionado) {
        this.contratoSeleccionado = contratoSeleccionado;

        // Crear una consulta SQL para obtener todos los registros para el contrato seleccionado
        String sql = "SELECT rd.*, contratos.numero_contrato, contratos.nombre_cliente, " +
                "   rd.entrada + IFNULL(( " +
                "       SELECT SUM(rd2.salida - rd2.defunciones) " +
                "       FROM registros_diarios rd2 " +
                "       WHERE rd2.contrato_id = (SELECT id FROM contratos WHERE numero_contrato = ?) " +
                "           AND rd2.nave = rd.nave " +
                "           AND rd2.fecha <= rd.fecha " +
                "       GROUP BY rd2.contrato_id, rd2.nave " +
                "   ), 0) AS total_animales " +
                "FROM registros_diarios rd " +
                "JOIN contratos ON rd.contrato_id = contratos.id " +
                "WHERE rd.contrato_id = (SELECT id FROM contratos WHERE numero_contrato = ?) " +
                "ORDER BY rd.fecha";

        try {
            PreparedStatement psSelectContratoID = Main.conn.prepareStatement(sql);
            psSelectContratoID.setInt(1, contratoSeleccionado.getNumeroContrato());
            psSelectContratoID.setInt(2, contratoSeleccionado.getNumeroContrato());
            ResultSet rs = psSelectContratoID.executeQuery();

            // Limpiar la lista antes de cargar los datos
            registros.clear();

            int totalAnimalesAnterior = 0; // Variable para almacenar el total de animales del día anterior
            while (rs.next()) {
                int entrada = rs.getInt("entrada");
                int salida = rs.getInt("salida");
                int bajas = rs.getInt("defunciones");

                // Calcular el total de animales del día actual
                int totalAnimales = totalAnimalesAnterior + entrada - salida - bajas;

                RegistroDiarioDetalle registro = new RegistroDiarioDetalle(
                        rs.getDate("fecha"),
                        entrada,
                        salida,
                        bajas,
                        totalAnimales);
                registros.add(registro);

                totalAnimalesAnterior = totalAnimales; // Actualizar el total de animales del día anterior
            }

            if (!registros.isEmpty()) {
                col_Dia.setCellValueFactory(new PropertyValueFactory<>("fecha"));
                col_Entrada.setCellValueFactory(new PropertyValueFactory<>("entrada"));
                col_Salida.setCellValueFactory(new PropertyValueFactory<>("salida"));
                col_Bajas.setCellValueFactory(new PropertyValueFactory<>("defunciones"));
                col_Total.setCellValueFactory(new PropertyValueFactory<>("total"));
                tabla_DatosNave.setItems(registros);
            } else {
                // Si no hay registros, puedes mostrar un mensaje o realizar alguna acción adicional
                System.out.println("No hay registros disponibles");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handler_AñadirDato(ActionEvent event) {
        Dialog<RegistroDiarioDetalle> dialog = new Dialog<>();
        dialog.setTitle("Añadir Registro Diario");
        dialog.setHeaderText("Añadir Registro Diario");

        ButtonType buttonTypeGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeGuardar, ButtonType.CANCEL);

        // Crear los campos de entrada de datos
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        TextField tfEntrada = new TextField();
        TextField tfSalida = new TextField();
        TextField tfBajas = new TextField();

        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Entrada:"), 0, 1);
        grid.add(tfEntrada, 1, 1);
        grid.add(new Label("Salida:"), 0, 2);
        grid.add(tfSalida, 1, 2);
        grid.add(new Label("Bajas:"), 0, 3);
        grid.add(tfBajas, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Validar los campos de entrada al hacer clic en "Guardar"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeGuardar) {
                try {
                    LocalDate fecha = datePicker.getValue();
                    int entrada = Integer.parseInt(tfEntrada.getText());
                    int salida = Integer.parseInt(tfSalida.getText());
                    int bajas = Integer.parseInt(tfBajas.getText());

                    // Verificar si el contrato seleccionado existe en la tabla contratos
                    String sqlSelectContrato = "SELECT id FROM contratos WHERE numero_contrato = ?";
                    PreparedStatement psSelectContrato = Main.conn.prepareStatement(sqlSelectContrato);
                    psSelectContrato.setInt(1, contratoSeleccionado.getNumeroContrato());
                    ResultSet rsContrato = psSelectContrato.executeQuery();
                    if (rsContrato.next()) {
                        int contratoId = rsContrato.getInt("id");

                        // Iniciar una transacción
                        try {
                            Main.conn.setAutoCommit(false);

                            // Calcular el total de animales del día anterior
                            int totalAnimalesDiaAnterior;
                            if (registros.isEmpty()) {
                                totalAnimalesDiaAnterior = 0;
                            } else {
                                totalAnimalesDiaAnterior = registros.get(registros.size() - 1).getTotal();
                            }

                            // Calcular el nuevo total de animales
                            int totalAnimales = totalAnimalesDiaAnterior + entrada - salida - bajas;

                            // Insertar el nuevo registro en la base de datos
                            String sqlInsert = "INSERT INTO registros_diarios (contrato_id, fecha, entrada, salida, defunciones, nave, animales_restantes) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement psInsert = Main.conn.prepareStatement(sqlInsert);
                            psInsert.setInt(1, contratoId);
                            psInsert.setDate(2, java.sql.Date.valueOf(fecha)); // Conversión de LocalDate a java.sql.Date
                            psInsert.setInt(3, entrada);
                            psInsert.setInt(4, salida);
                            psInsert.setInt(5, bajas);
                            psInsert.setInt(6, contratoSeleccionado.getNave());
                            psInsert.setInt(7, totalAnimales);
                            psInsert.executeUpdate();

                            // Obtener todos los registros de la base de datos
                            String sqlSelect = "SELECT * FROM registros_diarios WHERE contrato_id = ? AND nave = ? ORDER BY fecha ASC";
                            PreparedStatement psSelect = Main.conn.prepareStatement(sqlSelect);
                            psSelect.setInt(1, contratoId);
                            psSelect.setInt(2, contratoSeleccionado.getNave());
                            ResultSet rs = psSelect.executeQuery();

                            // Crear una lista de objetos RegistroDiarioDetalle con los registros obtenidos
                            List<RegistroDiarioDetalle> nuevosRegistros = new ArrayList<>();
                            while (rs.next()) {
                                Date fechaRegistro = rs.getDate("fecha");
                                int entradaRegistro = rs.getInt("entrada");
                                int salidaRegistro = rs.getInt("salida");
                                int defuncionesRegistro = rs.getInt("defunciones");
                                int animalesRestantesRegistro = rs.getInt("animales_restantes");

                                RegistroDiarioDetalle registro = new RegistroDiarioDetalle(fechaRegistro, entradaRegistro, salidaRegistro,
                                        defuncionesRegistro, animalesRestantesRegistro);
                                nuevosRegistros.add(registro);
                            }

                            // Actualizar la lista de registros
                            registros.clear();
                            registros.addAll(nuevosRegistros);

                            // Commit de la transacción
                            Main.conn.commit();
                            Main.conn.setAutoCommit(true);
                        } catch (SQLException e) {
                            // Manejar el error en caso de fallo en la transacción
                            e.printStackTrace();
                            Main.conn.rollback();
                            Main.conn.setAutoCommit(true);
                        }
                    } else {
                        System.out.println("Error, El contrato seleccionado no existe.");
                        return null;
                    }
                } catch (NumberFormatException | SQLException e) {
                    // Manejar el error en caso de que algún campo no sea un número válido
                    e.printStackTrace();
                    return null;
                }
            }

            return null;
        });

        dialog.showAndWait();
    }




    public void handler_BorrarDato(ActionEvent actionEvent) {
        RegistroDiarioDetalle registroSeleccionado = tabla_DatosNave.getSelectionModel().getSelectedItem();
        if (registroSeleccionado != null) {
            // Obtener el ID del registro seleccionado utilizando los demás valores del registro como criterio de búsqueda
            String sqlSelectID = "SELECT id FROM registros_diarios WHERE fecha = ? AND entrada = ? AND salida = ? AND defunciones = ?";
            try {
                PreparedStatement psSelectID = Main.conn.prepareStatement(sqlSelectID);
                psSelectID.setDate(1, Date.valueOf(registroSeleccionado.getFecha().toLocalDate()));
                psSelectID.setInt(2, registroSeleccionado.getEntrada());
                psSelectID.setInt(3, registroSeleccionado.getSalida());
                psSelectID.setInt(4, registroSeleccionado.getDefunciones());
                ResultSet rs = psSelectID.executeQuery();

                if (rs.next()) {
                    int registroID = rs.getInt("id");

                    // Ejecutar la consulta de eliminación utilizando el ID obtenido
                    String sqlDelete = "DELETE FROM registros_diarios WHERE id = ?";
                    PreparedStatement psDelete = Main.conn.prepareStatement(sqlDelete);
                    psDelete.setInt(1, registroID);
                    int rowsAffected = psDelete.executeUpdate();

                    if (rowsAffected > 0) {
                        // Eliminación exitosa, puedes realizar alguna acción adicional o mostrar un mensaje
                        System.out.println("Registro eliminado correctamente");

                        // Remover el registro de la lista y refrescar la tabla
                        registros.remove(registroSeleccionado);
                        tabla_DatosNave.refresh();
                    }
                } else {
                    // No se encontró el ID del registro
                    System.out.println("No se pudo encontrar el ID del registro");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handler_volver(ActionEvent actionEvent) {
        Stage stage = (Stage) Btn_volver.getScene().getWindow();
        stage.close();
    }
}
