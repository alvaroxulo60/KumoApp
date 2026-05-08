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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class GameFormController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtDesarrollador;
    @FXML private TextField txtAnio;
    @FXML private ListView<Genero> lvGeneros;
    @FXML private ListView<Plataforma> lvPlataformas;
    @FXML private TextField txtNota;
    @FXML private ComboBox<String> cbEstado;

    @FXML private ImageView imgPortada;
    // MODIFICADO: Guardamos la imagen como un array de bytes
    private byte[] bytesImagenSeleccionada = null;

    private final VideojuegoDAO videojuegoDAO = new VideojuegoDAOMysql();
    private final GeneroDAO generoDAO = new GeneroDAOMysql();
    private final PlataformaDAO plataformaDAO = new PlataformaDAOMysql();

    private Videojuego juegoEdicion = null;
    private boolean operacionExitosa = false;

    @FXML
    public void initialize() {
        lvGeneros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cbEstado.setItems(FXCollections.observableArrayList("Pendiente", "Jugando", "Completado", "Abandonado"));
        cbEstado.setValue("Pendiente");

        cargarCombos();

        txtAnio.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;
            }
            return null;
        }));
    }

    private void cargarCombos() {
        try {
            lvGeneros.setItems(FXCollections.observableArrayList(generoDAO.listarTodos()));
            lvPlataformas.setItems(FXCollections.observableArrayList(plataformaDAO.listarTodas()));
        } catch (AppException e) {
            mostrarAlerta("Error", "No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void handleSeleccionarPortada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Portada");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(txtTitulo.getScene().getWindow());

        if (file != null) {
            try {
                // MODIFICADO: Lee los bytes de la imagen directamente para la BD
                bytesImagenSeleccionada = Files.readAllBytes(file.toPath());

                // Muestra la imagen temporalmente leyendo la ruta local
                imgPortada.setImage(new Image(file.toURI().toString()));

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo cargar la imagen: " + e.getMessage());
            }
        }
    }

    public void setJuego(Videojuego v) {
        this.juegoEdicion = v;
        txtTitulo.setText(v.getTitulo());
        txtDesarrollador.setText(v.getDesarrollador());
        txtAnio.setText(String.valueOf(v.getAñoLanzamiento()));
        txtNota.setText(v.getNotaPersonal() != null ? v.getNotaPersonal() : "");
        cbEstado.setValue(v.getEstado() != null ? v.getEstado() : "Pendiente");

        if (v.getGeneros() != null) {
            for (Genero g : v.getGeneros()) {
                lvGeneros.getItems().stream().filter(item -> item.id() == g.id()).findFirst().ifPresent(lvGeneros.getSelectionModel()::select);
            }
        }
        if (v.getPlataformas() != null) {
            for (Plataforma p : v.getPlataformas()) {
                lvPlataformas.getItems().stream().filter(item -> item.id() == p.id()).findFirst().ifPresent(lvPlataformas.getSelectionModel()::select);
            }
        }

        // MODIFICADO: Cargar la portada desde los bytes almacenados en el objeto
        if (v.getPortada() != null && v.getPortada().length > 0) {
            bytesImagenSeleccionada = v.getPortada();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytesImagenSeleccionada);
            imgPortada.setImage(new Image(bis));
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
                v.setNotaPersonal(txtNota.getText());
                v.setEstado(cbEstado.getValue());

                // MODIFICADO: Asigna el array de bytes de la imagen al objeto
                v.setPortada(bytesImagenSeleccionada);

                v.setGeneros(new ArrayList<>(lvGeneros.getSelectionModel().getSelectedItems()));
                v.setPlataformas(new ArrayList<>(lvPlataformas.getSelectionModel().getSelectedItems()));

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
                    lvGeneros.getSelectionModel().getSelectedItems().isEmpty() ||
                    lvPlataformas.getSelectionModel().getSelectedItems().isEmpty()) {
                mostrarAlerta("Campos vacíos", "Por favor, rellena título, año y selecciona al menos un género y plataforma.");
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