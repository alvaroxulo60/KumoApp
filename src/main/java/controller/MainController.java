package controller;

import DAO.VideojuegoDAOMysql;
import io.App;
import io.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Videojuego;
import exception.AppException;
import java.io.IOException;

public class MainController {
    @FXML private TableView<Videojuego> tablaJuegos;
    @FXML private TableColumn<Videojuego, String> colTitulo;
    @FXML private TableColumn<Videojuego, String> colDesarrollador;
    @FXML private TableColumn<Videojuego, Integer> colAnio;

    private VideojuegoDAOMysql gameDAO = new VideojuegoDAOMysql();

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrollador.setCellValueFactory(new PropertyValueFactory<>("desarrollador"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("añoLanzamiento"));
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        if (Sesion.getUsuario() != null) {
            tablaJuegos.getItems().setAll(gameDAO.listarPorUsuario(Sesion.getUsuario().getIdUsuario()));
        }
    }

    @FXML
    private void eliminarJuego() {
        Videojuego sel = tablaJuegos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            try {
                gameDAO.eliminarVideojuego(sel.getIdVideojuego());
                cargarDatosUsuario();
            } catch (AppException e) {
                mostrarAlerta("Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Sesion.logout();
            App.setRoot("LoginView");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t);
        a.setContentText(m);
        a.showAndWait();
    }
}