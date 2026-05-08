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
import javafx.scene.image.ImageView; // <-- AÑADIDO: Importación necesaria
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class GameFormController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtDesarrollador;
    @FXML private TextField txtAnio;
    @FXML private ListView<Genero> lvGeneros;
    @FXML private ListView<Plataforma> lvPlataformas;
    @FXML private TextField txtNota;
    @FXML private ComboBox<String> cbEstado;

    // <-- AÑADIDO: Declaración de las variables para la portada
    @FXML private ImageView imgPortada;
    private String rutaImagenSeleccionada = null;

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

        // Forzar que el campo del año solo acepte números
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

        // Abre el diálogo para buscar el archivo
        File file = fileChooser.showOpenDialog(txtTitulo.getScene().getWindow());

        if (file != null) {
            try {
                // Crea la carpeta "portadas" en la raíz del proyecto si no existe
                Path carpetaDestino = Paths.get("portadas");
                if (!Files.exists(carpetaDestino)) {
                    Files.createDirectories(carpetaDestino);
                }

                // Copia la imagen a la carpeta portadas (reemplaza si ya existe una con el mismo nombre)
                Path destino = carpetaDestino.resolve(file.getName());
                Files.copy(file.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

                // Guarda la ruta relativa y la muestra en el ImageView
                rutaImagenSeleccionada = destino.toString();
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

        // <-- AÑADIDO: Cargar la portada en el formulario si el juego ya tiene una
        if (v.getRutaPortada() != null && !v.getRutaPortada().isEmpty()) {
            rutaImagenSeleccionada = v.getRutaPortada();
            File file = new File(rutaImagenSeleccionada);
            if (file.exists()) {
                imgPortada.setImage(new Image(file.toURI().toString()));
            }
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

                // <-- AÑADIDO: Guardar la ruta seleccionada en el objeto
                v.setRutaPortada(rutaImagenSeleccionada);

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