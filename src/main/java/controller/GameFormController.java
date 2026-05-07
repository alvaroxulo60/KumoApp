package controller;

import DAO.GeneroDAOMysql;
import DAO.PlataformaDAOMysql;
import DAO.VideojuegoDAOMysql;
import exception.AppException;
import interfacesDAO.GeneroDAO;
import interfacesDAO.PlataformaDAO;
import interfacesDAO.VideojuegoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.util.ArrayList;
import java.util.List;

public class GameFormController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtDesarrollador;
    @FXML private TextField txtAnio;
    @FXML private ComboBox<Genero> cbGenero;
    @FXML private ComboBox<Plataforma> cbPlataforma;

    private final VideojuegoDAO videojuegoDAO = new VideojuegoDAOMysql();
    private final GeneroDAO generoDAO = new GeneroDAOMysql();
    private final PlataformaDAO plataformaDAO = new PlataformaDAOMysql();

    private Videojuego juegoEdicion = null;
    private boolean operacionExitosa = false;

    @FXML
    public void initialize() {
        cargarCombos();
    }

    private void cargarCombos() {
        try {
            cbGenero.setItems(FXCollections.observableArrayList(generoDAO.listarTodos()));
            cbPlataforma.setItems(FXCollections.observableArrayList(plataformaDAO.listarTodas()));
        } catch (AppException e) {
            mostrarAlerta("Error", "No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    /**
     * Rellena el formulario con los datos de un juego existente para editarlo.
     */
    public void setJuego(Videojuego v) {
        this.juegoEdicion = v;
        txtTitulo.setText(v.getTitulo());
        txtDesarrollador.setText(v.getDesarrollador());
        txtAnio.setText(String.valueOf(v.getAñoLanzamiento()));

        if (v.getGeneros() != null && !v.getGeneros().isEmpty()) {
            cbGenero.setValue(v.getGeneros().get(0));
        }
        if (v.getPlataformas() != null && !v.getPlataformas().isEmpty()) {
            cbPlataforma.setValue(v.getPlataformas().get(0));
        }
    }

    @FXML
    private void handleGuardar() {
        if (validar()) {
            try {
                Videojuego v = (juegoEdicion == null) ? new Videojuego() : juegoEdicion;
                v.setTitulo(txtTitulo.getText());
                v.setDesarrollador(txtDesarrollador.getText());
                v.setAñoLanzamiento(Integer.parseInt(txtAnio.getText()));

                List<Genero> gs = new ArrayList<>(); gs.add(cbGenero.getValue());
                v.setGeneros(gs);

                List<Plataforma> ps = new ArrayList<>(); ps.add(cbPlataforma.getValue());
                v.setPlataformas(ps);

                if (juegoEdicion == null) {
                    videojuegoDAO.insertar(v);
                } else {
                    videojuegoDAO.actualizarVideojuego(v);
                }

                operacionExitosa = true;
                cerrar();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al guardar: " + e.getMessage());
            }
        }
    }

    private boolean validar() {
        try {
            if (txtTitulo.getText().isEmpty() || txtAnio.getText().isEmpty() ||
                    cbGenero.getValue() == null || cbPlataforma.getValue() == null) {
                mostrarAlerta("Campos vacíos", "Por favor, rellena todos los campos obligatorios.");
                return false;
            }
            Integer.parseInt(txtAnio.getText());
            return true;
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "El año debe ser un número válido.");
            return false;
        }
    }

    @FXML private void handleCancelar() { cerrar(); }

    private void cerrar() { ((Stage) txtTitulo.getScene().getWindow()).close(); }

    public boolean isOperacionExitosa() { return operacionExitosa; }

    private void mostrarAlerta(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(c); a.showAndWait();
    }
}