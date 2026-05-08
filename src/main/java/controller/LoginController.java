package controller;

import DAO.UsuarioDAOMysql;
import io.App;
import io.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Usuario;

import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnEntrar;

    @FXML
    public void initialize() {
        txtEmail.setOnAction(event -> handleLogin());
        txtPassword.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Introduce tus credenciales.");
            return;
        }

        UsuarioDAOMysql userDAO = new UsuarioDAOMysql();
        try {
            List<Usuario> usuarios = userDAO.listarTodos();

            Usuario encontrado = usuarios.stream()
                    .filter(u -> email.equalsIgnoreCase(u.getEmail()) && pass.equals(u.getPassword()))
                    .findFirst()
                    .orElse(null);

            if (encontrado != null) {
                Sesion.setUsuario(encontrado);
                App.setRoot("MainView");
            } else {
                mostrarAlerta("Error de acceso", "Email o contraseña incorrectos.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error real en la consola roja de tu IDE
            String causa = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            mostrarAlerta("Fallo en la aplicación", "Error real al cargar: " + causa);
        }
    }

    @FXML
    private void abrirRegistro() { // Cambiado para que funcione con el Hyperlink
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Crear Nueva Cuenta - KumoApp");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Bloquea la de atrás hasta cerrar esta
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la pantalla de registro.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}