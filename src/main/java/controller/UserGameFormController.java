package controller;

import DAO.VideojuegoDAOMysql;
import exception.AppException;
import io.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Videojuego;
import javafx.util.StringConverter;

public class UserGameFormController {

    @FXML private ComboBox<Videojuego> cbJuegoCatalogo;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtNota;

    private final VideojuegoDAOMysql gameDAO = new VideojuegoDAOMysql();
    private ObservableList<Videojuego> listaMaestra;
    private boolean operacionExitosa = false;

    @FXML
    public void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Pendiente", "Jugando", "Completado", "Abandonado"));

        // 1. Configurar el conversor para mostrar títulos
        cbJuegoCatalogo.setConverter(new StringConverter<Videojuego>() {
            @Override public String toString(Videojuego v) {
                return v == null ? "" : v.getTitulo();
            }
            @Override public Videojuego fromString(String s) {
                return listaMaestra.stream().filter(v -> v.getTitulo().equalsIgnoreCase(s)).findFirst().orElse(null);
            }
        });

        // 2. Cargar datos y configurar el filtro predictivo
        try {
            listaMaestra = FXCollections.observableArrayList(gameDAO.listarTodos());
            FilteredList<Videojuego> listaFiltrada = new FilteredList<>(listaMaestra, p -> true);

            // Escuchar lo que el usuario escribe en el campo de texto del ComboBox
            cbJuegoCatalogo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {

                // --- SOLUCIÓN AL BUCLE INFINITO ---
                // Si el nuevo texto es exactamente el título del juego seleccionado, no hacemos nada.
                Videojuego seleccionado = cbJuegoCatalogo.getSelectionModel().getSelectedItem();
                if (seleccionado != null && seleccionado.getTitulo().equals(newVal)) {
                    return;
                }
                // ----------------------------------

                listaFiltrada.setPredicate(juego -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    return juego.getTitulo().toLowerCase().contains(newVal.toLowerCase());
                });

                // Mostrar el desplegable mientras se escribe
                cbJuegoCatalogo.show();
            });

            cbJuegoCatalogo.setItems(listaFiltrada);
        } catch (AppException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuardar() {
        Videojuego sel = cbJuegoCatalogo.getValue();

        // Si el usuario escribió y no hizo click en la lista, buscamos coincidencia exacta
        if (sel == null && cbJuegoCatalogo.getEditor().getText() != null) {
            String texto = cbJuegoCatalogo.getEditor().getText();
            sel = listaMaestra.stream().filter(v -> v.getTitulo().equalsIgnoreCase(texto)).findFirst().orElse(null);
        }

        if (sel == null) {
            mostrarAlerta("Error", "Debes seleccionar un juego válido del catálogo.");
            return;
        }

        try {
            gameDAO.anadirJuegoColeccion(Sesion.getUsuario().getIdUsuario(), sel.getIdVideojuego(), cbEstado.getValue(), txtNota.getText());
            operacionExitosa = true;
            cerrar();
        } catch (AppException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    public void setJuegoEdicion(Videojuego v) {
        cbJuegoCatalogo.setValue(v);
        cbJuegoCatalogo.setDisable(true);
        txtNota.setText(v.getNotaPersonal() != null ? v.getNotaPersonal() : "0");
        cbEstado.setValue(v.getEstado() != null ? v.getEstado() : "Pendiente");
    }

    @FXML
    private void handleCancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage)txtNota.getScene().getWindow()).close();
    }

    public boolean isOperacionExitosa() {
        return operacionExitosa;
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t);
        a.setContentText(m);
        a.showAndWait();
    }
}