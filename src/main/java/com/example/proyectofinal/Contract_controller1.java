package com.example.proyectofinal;


import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;



public class Contract_controller1 {

    @FXML
    private Button btn_Añadir;

    @FXML
    private Button btn_Borrar;

    @FXML
    private Button btn_EnviarPDF;

    @FXML
    private Button btn_GenerarPDF;

    @FXML
    private Button btn_volver;

    @FXML
    private TableColumn<RegistroDiario, Integer> col_nContrato;
    @FXML
    private TableView<Contrato> tabla_Contratos;

    private Data_controller dataController = new Data_controller();
    public ObservableList<Contrato> contratos = FXCollections.observableArrayList();

    public void inicializarDatos1() {
        // Crear una consulta SQL para obtener los registros de la nave 1
        String sql = "SELECT * FROM contratos WHERE nave = 1";

        try {
            Statement stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Contrato contrato = new Contrato(rs.getInt("numero_contrato"), rs.getInt("nave"));
                contratos.add(contrato);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        col_nContrato.setCellValueFactory(new PropertyValueFactory<>("numeroContrato"));

        tabla_Contratos.setItems(contratos);

        tabla_Contratos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Contrato contrato = tabla_Contratos.getSelectionModel().getSelectedItem();
                try {
                    handlerDetalleContrato(contrato);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Contrato seleccionado: " + contrato.getNumeroContrato());
            }
        });
    }
    public void handlerDetalleContrato(Contrato contratoSeleccionado) throws IOException {
        contratoSeleccionado.setNave(1);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Data_view.fxml"));
            Parent root = loader.load();
            Data_controller data_controller = loader.getController();
            data_controller.inicializarDatosDetalle1(contratoSeleccionado);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handler_AñadirContrato(ActionEvent event) {
        // Crear un cuadro de diálogo para ingresar el número del contrato
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir contrato");
        dialog.setHeaderText(null);
        dialog.setContentText("Por favor ingrese el número del nuevo contrato:");

// Obtener el resultado del cuadro de diálogo
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String numeroContrato = result.get();

            // Validar que el número del contrato no esté vacío and cumpla con las condiciones de longitud y contenido
            if (!numeroContrato.isEmpty() && numeroContrato.matches("^\\d{6}$")) {
                // Validar que el número del contrato no exista ya en la lista
                if (contratos.stream().noneMatch(c -> String.valueOf(c.getNumeroContrato()).equals(numeroContrato))) {
                    try {
                        // Crear un cuadro de diálogo para elegir el cliente asociado al contrato

                        ChoiceDialog<String> clienteDialog = new ChoiceDialog<>("", new Clientes().getClientes().stream().map(Cliente::getNombre).collect(Collectors.toList()));
                        clienteDialog.setTitle("Añadir contrato");
                        clienteDialog.setHeaderText(null);
                        clienteDialog.setContentText("Por favor elija al cliente asociado al contrato:");

                        // Obtener el resultado del cuadro de diálogo de clientes
                        Optional<String> clienteResult = clienteDialog.showAndWait();
                        if (clienteResult.isPresent()) {
                            String nombreCliente = clienteResult.get();

                            // Insertar el nuevo contrato en la base de datos
                            String sqlInsertContrato = "INSERT INTO contratos (numero_contrato, nave, nombre_cliente) VALUES (?, ?, ?)";
                            PreparedStatement psInsertContrato = Main.conn.prepareStatement(sqlInsertContrato, Statement.RETURN_GENERATED_KEYS);
                            psInsertContrato.setString(1, numeroContrato);
                            psInsertContrato.setInt(2, 1);
                            psInsertContrato.setString(3, nombreCliente);
                            psInsertContrato.executeUpdate();

                            // Almacenar el número del contrato que se acaba de insertar
                            int nuevoContratoId;
                            try (ResultSet generatedKeys = psInsertContrato.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    nuevoContratoId = generatedKeys.getInt(1);
                                } else {
                                    throw new SQLException("Fallo creacion");
                                }
                            }
                            // Agregar el nuevo contrato a la lista
                            Contrato contrato = new Contrato(Integer.parseInt(numeroContrato), 1);
                            contratos.add(contrato);
                            tabla_Contratos.refresh();

                            // Insertar un registro diario asociado al nuevo contrato
                            String sqlInsertRegistro = "INSERT INTO registros_diarios (contrato_id, fecha, entrada, salida, defunciones, nave, animales_restantes) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement psInsertRegistro = Main.conn.prepareStatement(sqlInsertRegistro);
                            psInsertRegistro.setInt(1, nuevoContratoId);
                            psInsertRegistro.setDate(2, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
                            psInsertRegistro.setInt(3, 0);
                            psInsertRegistro.setInt(4, 0);
                            psInsertRegistro.setInt(5, 0);
                            psInsertRegistro.setInt(6, 1);
                            psInsertRegistro.setInt(7, 0);
                            psInsertRegistro.executeUpdate();
                            cargarRegistrosDiarios();
                        } else {
                            // Mostrar un mensaje de error si no se eligió ningún cliente
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Debe elegir un cliente para asociar al contrato.");
                            alert.showAndWait();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Mostrar un mensaje de error si el número del contrato ya existe
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("El número del contrato ya existe en la lista.");
                    alert.showAndWait();
                }
            } else {
                // Mostrar un mensaje de error si el número del contrato está vacío o no cumple con las condiciones de longitud y contenido
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("El número del contrato debe tener 6 dígitos.");
                alert.showAndWait();
            }
        }
    }

    private void cargarRegistrosDiarios() {
        contratos.clear();
        // Crear una consulta SQL para obtener los registros de la nave 1
        String sql = "SELECT * FROM contratos WHERE nave = 1";

        try {
            Statement stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Contrato contrato = new Contrato(rs.getInt("numero_contrato"));
                contratos.add(contrato);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        col_nContrato.setCellValueFactory(new PropertyValueFactory<>("numeroContrato"));

        tabla_Contratos.setItems(contratos);
    }
    @FXML
    void handler_BorrarContrato(ActionEvent event) {
        // Obtener el contrato seleccionado en la tabla
        Contrato contrato = tabla_Contratos.getSelectionModel().getSelectedItem();
        if (contrato != null) {
            // Preguntar al usuario si realmente quiere borrar el contrato
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea borrar el contrato " + contrato.getNumeroContrato() + "?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {
                    Main.conn.setAutoCommit(false);
                    // Obtener el contrato_id correspondiente al contrato seleccionado
                    String sqlSelectContratoID = "SELECT id FROM contratos WHERE numero_contrato = ?";
                    PreparedStatement psSelectContratoID = Main.conn.prepareStatement(sqlSelectContratoID);
                    psSelectContratoID.setInt(1, contrato.getNumeroContrato());
                    ResultSet rsContratoID = psSelectContratoID.executeQuery();

                    if (rsContratoID.next()) {
                        int contrato_id = rsContratoID.getInt("id");
                        String sqlDeleteRegistros = "DELETE FROM registros_diarios WHERE contrato_id = ?";
                        PreparedStatement psDeleteRegistros = Main.conn.prepareStatement(sqlDeleteRegistros);
                        psDeleteRegistros.setInt(1, contrato_id);
                        psDeleteRegistros.executeUpdate();


                        String sqlDeleteContrato = "DELETE FROM contratos WHERE id = ?";
                        PreparedStatement psDeleteContrato = Main.conn.prepareStatement(sqlDeleteContrato);
                        psDeleteContrato.setInt(1, contrato_id);
                        psDeleteContrato.executeUpdate();
                        Main.conn.commit();

                        contratos.remove(contrato);
                        tabla_Contratos.refresh();
                        Main.conn.setAutoCommit(true);
                    } else {
                        // Manejar el caso en el que no se encuentran filas
                    }
                    rsContratoID.close();
                    psSelectContratoID.close();


                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @FXML
    void handler_EnviarPDF(ActionEvent event) {
        Contrato contratoSeleccionado = tabla_Contratos.getSelectionModel().getSelectedItem();
        if (contratoSeleccionado != null) {
            // Obtener registros diarios del contrato seleccionado desde la base de datos
            List<RegistroDiarioDetalle> registros = obtenerRegistrosDiarios(contratoSeleccionado.getNumeroContrato());

            // Crear el documento PDF en memoria
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);

            try {
                // Crear el objeto PdfWriter para escribir el contenido en el ByteArrayOutputStream
                PdfWriter.getInstance(document, baos);

                // Abrir el documento
                document.open();

                // Agregar el título al documento
                Paragraph titulo = new Paragraph("Registros diarios del contrato: " + contratoSeleccionado.getNumeroContrato());
                titulo.setAlignment(Element.ALIGN_CENTER);
                titulo.setSpacingAfter(20f);
                document.add(titulo);

                // Crear la tabla para los registros diarios
                PdfPTable tablaRegistros = new PdfPTable(5); // 5 columnas para fecha, entrada, salida, defunciones y total
                tablaRegistros.setWidthPercentage(100);
                tablaRegistros.setSpacingBefore(10f);
                tablaRegistros.setSpacingAfter(10f);

                // Agregar encabezados de tabla
                String[] encabezados = {"Fecha", "Entrada", "Salida", "Defunciones", "Total"};
                for (String encabezado : encabezados) {
                    PdfPCell celdaEncabezado = new PdfPCell(new Paragraph(encabezado));
                    celdaEncabezado.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaEncabezado);
                }

                // Agregar los datos de los registros diarios a la tabla
                for (RegistroDiarioDetalle registro : registros) {
                    PdfPCell celdaFecha = new PdfPCell(new Paragraph(String.valueOf(registro.getFecha())));
                    celdaFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaFecha);

                    PdfPCell celdaEntrada = new PdfPCell(new Paragraph(String.valueOf(registro.getEntrada())));
                    celdaEntrada.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaEntrada);

                    PdfPCell celdaSalida = new PdfPCell(new Paragraph(String.valueOf(registro.getSalida())));
                    celdaSalida.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaSalida);

                    PdfPCell celdaDefunciones = new PdfPCell(new Paragraph(String.valueOf(registro.getDefunciones())));
                    celdaDefunciones.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaDefunciones);

                    PdfPCell celdaTotal = new PdfPCell(new Paragraph(String.valueOf(registro.getTotal())));
                    celdaTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaRegistros.addCell(celdaTotal);
                }

                // Agregar la tabla al documento
                document.add(tablaRegistros);

                // Cerrar el documento
                document.close();

                // Mostrar el diálogo para ingresar el correo electrónico del destinatario
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Enviar PDF por correo");
                dialog.setHeaderText("Ingrese el correo electrónico del destinatario");
                dialog.setContentText("Correo electrónico:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(destinatario -> {
                    // Enviar el PDF por correo electrónico
                    enviarCorreoElectronico(destinatario, baos.toByteArray());
                });

                System.out.println("PDF generado y enviado correctamente.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No se ha seleccionado ningún contrato.");
        }
    }
    private void enviarCorreoElectronico(String destinatario, byte[] archivoAdjunto) {
        // Configurar las propiedades del servidor de correo
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");

        // Configurar las credenciales del remitente
        // Leer las credenciales del remitente desde el archivo settings.properties
        Properties config = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/settings.properties")) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String remitente = config.getProperty("remitente");
        String password = config.getProperty("contraseña");

        // Crear una sesión de correo electrónico
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, password);
            }
        });

        try {
            // Crear el mensaje de correo electrónico
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Registros diarios del contrato");
            message.setText("Adjunto se encuentra el archivo PDF con los registros diarios del contrato.");

            // Adjuntar el archivo PDF al mensaje
            MimeBodyPart adjunto = new MimeBodyPart();
            adjunto.setDataHandler(new DataHandler(new ByteArrayDataSource(archivoAdjunto, "application/pdf")));
            adjunto.setFileName("registros_diarios.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(adjunto);

            message.setContent(multipart);

            // Enviar el mensaje de correo electrónico
            Transport.send(message);

            System.out.println("Correo electrónico enviado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handler_GenerarPDF(ActionEvent event) {
        Contrato contratoSeleccionado = tabla_Contratos.getSelectionModel().getSelectedItem();
        if (contratoSeleccionado != null) {
            // Obtener registros diarios del contrato seleccionado desde la base de datos
            List<RegistroDiarioDetalle> registros = obtenerRegistrosDiarios(contratoSeleccionado.getNumeroContrato());

            // Crear el documento PDF
            Document document = new Document(PageSize.A4);

            try {
                // Crear un FileChooser para seleccionar la ubicación y el nombre del archivo PDF
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));

                // Mostrar el diálogo de guardar archivo
                File file = fileChooser.showSaveDialog(new Stage());

                if (file != null) {
                    // Crear el objeto PdfWriter para escribir el contenido en el archivo seleccionado
                    PdfWriter.getInstance(document, new FileOutputStream(file));

                    // Abrir el documento
                    document.open();

                    // Agregar el título al documento
                    Paragraph titulo = new Paragraph("Registros diarios del contrato: " + contratoSeleccionado.getNumeroContrato());
                    titulo.setAlignment(Element.ALIGN_CENTER);
                    titulo.setSpacingAfter(20f);
                    document.add(titulo);

                    // Crear la tabla para los registros diarios
                    PdfPTable tablaRegistros = new PdfPTable(5); // 5 columnas para fecha, entrada, salida, defunciones y total
                    tablaRegistros.setWidthPercentage(100);
                    tablaRegistros.setSpacingBefore(10f);
                    tablaRegistros.setSpacingAfter(10f);

                    // Agregar encabezados de tabla
                    String[] encabezados = {"Fecha", "Entrada", "Salida", "Defunciones", "Total"};
                    for (String encabezado : encabezados) {
                        PdfPCell celdaEncabezado = new PdfPCell(new Paragraph(encabezado));
                        celdaEncabezado.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaEncabezado);
                    }

                    // Agregar los datos de los registros diarios a la tabla
                    for (RegistroDiarioDetalle registro : registros) {
                        PdfPCell celdaFecha = new PdfPCell(new Paragraph(String.valueOf(registro.getFecha())));
                        celdaFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaFecha);

                        PdfPCell celdaEntrada = new PdfPCell(new Paragraph(String.valueOf(registro.getEntrada())));
                        celdaEntrada.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaEntrada);

                        PdfPCell celdaSalida = new PdfPCell(new Paragraph(String.valueOf(registro.getSalida())));
                        celdaSalida.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaSalida);

                        PdfPCell celdaDefunciones = new PdfPCell(new Paragraph(String.valueOf(registro.getDefunciones())));
                        celdaDefunciones.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaDefunciones);

                        PdfPCell celdaTotal = new PdfPCell(new Paragraph(String.valueOf(registro.getTotal())));
                        celdaTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
                        tablaRegistros.addCell(celdaTotal);
                    }

                    // Agregar la tabla al documento
                    document.add(tablaRegistros);

                    // Cerrar el documento
                    document.close();

                    System.out.println("PDF generado correctamente.");
                } else {
                    System.out.println("No se ha seleccionado una ubicación para guardar el PDF.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No se ha seleccionado ningún contrato.");
        }
    }
    private List<RegistroDiarioDetalle> obtenerRegistrosDiarios(int numeroContrato) {
        List<RegistroDiarioDetalle> registros = new ArrayList<>();

        try {
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

            PreparedStatement statement = Main.conn.prepareStatement(sql);
            statement.setInt(1, numeroContrato);
            statement.setInt(2, numeroContrato);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Date fecha = resultSet.getDate("fecha");
                int entrada = resultSet.getInt("entrada");
                int salida = resultSet.getInt("salida");
                int defunciones = resultSet.getInt("defunciones");
                int total = resultSet.getInt("total_animales");

                // Crear un objeto RegistroDiarioDetalle con los datos obtenidos
                RegistroDiarioDetalle registro = new RegistroDiarioDetalle(fecha, entrada, salida, defunciones, total);
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return registros;
    }
    public void handler_Volver(ActionEvent event) {
        Stage stage = (Stage) btn_volver.getScene().getWindow();
        stage.close();
    }

}
