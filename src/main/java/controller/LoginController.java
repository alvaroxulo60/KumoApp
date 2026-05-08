package controller;

import DAO.UsuarioDAOMysql;
import io.App;
import io.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Usuario;
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

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}