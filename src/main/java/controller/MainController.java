package controller;

import DAO.VideojuegoDAOMysql;
import exception.AppException;
import io.Sesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.io.IOException;
import java.util.stream.Collectors;

public class MainController {
    @FXML private TableView<Videojuego> tablaJuegos;
    @FXML private TableColumn<Videojuego, String> colTitulo, colDesarrollador, colGeneros, colPlataformas;
    @FXML private TableColumn<Videojuego, Integer> colAnio;

    private final VideojuegoDAOMysql gameDAO = new VideojuegoDAOMysql();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrollador.setCellValueFactory(new PropertyValueFactory<>("desarrollador"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("añoLanzamiento"));

        // Mapeo de listas de objetos a un String separado por comas para la visualización
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
                mostrarAlerta("Error", "No se pudieron cargar los juegos: " + e.getMessage());
            }
        }
    }

    @FXML
    private void abrirAnadir() {
        mostrarFormulario(null);
    }

    @FXML
    private void abrirEditar() {
        Videojuego sel = tablaJuegos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            mostrarFormulario(sel);
        } else {
            mostrarAlerta("Selección necesaria", "Por favor, selecciona un juego de la tabla para editar.");
        }
    }

    private void mostrarFormulario(Videojuego v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GameFormView.fxml"));
            Parent root = loader.load();

            GameFormController controller = loader.getController();
            if (v != null) controller.setJuego(v);

            Stage stage = new Stage();
            stage.setTitle(v == null ? "Añadir Videojuego" : "Editar Videojuego");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Si el formulario confirma que se guardó información, refrescamos la tabla
            if (controller.isOperacionExitosa()) {
                cargarDatos();
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de sistema", "No se pudo abrir el formulario.");
        }
    }

    @FXML
    private void eliminarJuego() {
        Videojuego sel = tablaJuegos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            try {
                gameDAO.eliminarVideojuego(sel.getIdVideojuego());
                cargarDatos();
            } catch (AppException e) {
                mostrarAlerta("Error al eliminar", e.getMessage());
            }
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
}