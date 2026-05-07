package controller;

import DAO.VideojuegoDAOMysql;
import io.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Videojuego;
import exception.AppException;

public class MainController {
    @FXML private TableView<Videojuego> tablaJuegos;
    @FXML private TableColumn<Videojuego, String> colTitulo;
    @FXML private TableColumn<Videojuego, String> colDesarrollador;
    @FXML private TableColumn<Videojuego, Integer> colAnio;

    private VideojuegoDAOMysql gameDAO = new VideojuegoDAOMysql();

    @FXML
    public void initialize() {
        // Configurar cómo se muestran los datos en la tabla
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrollador.setCellValueFactory(new PropertyValueFactory<>("desarrollador"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("añoLanzamiento"));

        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        // Solo cargamos los juegos del usuario que inició sesión
        int idActual = Sesion.getUsuario().getIdUsuario();
        tablaJuegos.getItems().setAll(gameDAO.listarPorUsuario(idActual));
    }

    @FXML
    private void eliminarJuego() {
        Videojuego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try {
                gameDAO.eliminarVideojuego(seleccionado.getIdVideojuego()); //
                cargarDatosUsuario(); // Refrescar
            } catch (AppException e) {
                mostrarAlerta("Error al Guardar", e.getMessage(), Alert.AlertType.ERROR);            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}