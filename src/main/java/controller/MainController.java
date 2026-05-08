package controller;

import DAO.VideojuegoDAOMysql;
import exception.AppException;
import io.Sesion;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.io.IOException;
import java.util.stream.Collectors;

public class MainController {
    @FXML private TableView<Videojuego> tablaJuegos;
    @FXML private TableColumn<Videojuego, String> colTitulo, colDesarrollador, colGeneros, colPlataformas, colEstado, colNota;
    @FXML private TableColumn<Videojuego, Integer> colAnio;

    private final VideojuegoDAOMysql gameDAO = new VideojuegoDAOMysql();

    @FXML private ImageView imgPortadaMain;
    @FXML private Label lblTituloSeleccionado;
    @FXML private Button btnAdminCrear; // Declaración del botón admin de tu FXML

    @FXML
    public void initialize() {
        // Validación de Administrador
        boolean esAdmin = Sesion.getUsuario().isAdmin();
        if (btnAdminCrear != null) {
            btnAdminCrear.setVisible(esAdmin);
            btnAdminCrear.setManaged(esAdmin);
        }

        configurarColumnas();
        cargarDatos();

        tablaJuegos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lblTituloSeleccionado.setText(newValue.getTitulo());

                if (newValue.getPortada() != null && newValue.getPortada().length > 0) {
                    java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(newValue.getPortada());
                    imgPortadaMain.setImage(new javafx.scene.image.Image(bis));
                } else {
                    imgPortadaMain.setImage(null);
                }
            } else {
                imgPortadaMain.setImage(null);
                lblTituloSeleccionado.setText("Selecciona un juego");
            }
        });
    }

    private void configurarColumnas() {
        tablaJuegos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrollador.setCellValueFactory(new PropertyValueFactory<>("desarrollador"));
        colAnio.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getAñoLanzamiento()).asObject());
        colNota.setCellValueFactory(new PropertyValueFactory<>("notaPersonal"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colGeneros.setCellValueFactory(cd -> {
            String generos = cd.getValue().getGeneros().stream()
                    .map(Genero::nombre)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(generos);
        });

        colPlataformas.setCellValueFactory(cd -> {
            String plataformas = cd.getValue().getPlataformas().stream()
                    .map(Plataforma::nombre)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(plataformas);
        });
    }

    private void cargarDatos() {
        if (Sesion.getUsuario() != null) {
            try {
                tablaJuegos.setItems(FXCollections.observableArrayList(
                        gameDAO.listarPorUsuario(Sesion.getUsuario().getIdUsuario())
                ));
            } catch (AppException e) {
                Platform.runLater(() -> mostrarAlerta("Error al cargar", "Motivo: " + e.getMessage()));
            }
        }
    }

    @FXML
    private void abrirAnadir() {
        mostrarFormularioUsuario(null);
    }

    @FXML
    private void abrirEditar() {
        Videojuego sel = tablaJuegos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            mostrarFormularioUsuario(sel);
        } else {
            mostrarAlerta("Selección necesaria", "Por favor, selecciona un juego de la tabla para editar tu nota.");
        }
    }

    // Método para usuarios normales (Abre el nuevo controlador)
    private void mostrarFormularioUsuario(Videojuego v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserGameFormView.fxml"));
            Parent root = loader.load();
            UserGameFormController controller = loader.getController();

            if (v != null) controller.setJuegoEdicion(v);

            Stage stage = new Stage();
            stage.setTitle(v == null ? "Añadir a mi lista" : "Editar Nota y Estado");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isOperacionExitosa()) {
                cargarDatos();
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de sistema", "No se pudo abrir el formulario.");
        }
    }

    // Método EXCLUSIVO para Administradores (Abre tu formulario original)
    @FXML
    private void abrirFormularioAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameFormView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Panel de Administración - Crear Juego Global");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de sistema", "No se pudo abrir el panel de administración.");
        }
    }

    @FXML
    private void eliminarJuego() {
        Videojuego sel = tablaJuegos.getSelectionModel().getSelectedItem();
        if (sel != null && Sesion.getUsuario() != null) {
            try {
                gameDAO.eliminarJuegoDeUsuario(Sesion.getUsuario().getIdUsuario(), sel.getIdVideojuego());
                cargarDatos();
            } catch (AppException e) {
                mostrarAlerta("Error al eliminar", e.getMessage());
            }
        } else {
            mostrarAlerta("Aviso", "Selecciona un juego para quitarlo de tu biblioteca.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Sesion.logout();
            io.App.setRoot("LoginView");
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo volver a la pantalla de inicio.");
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
}