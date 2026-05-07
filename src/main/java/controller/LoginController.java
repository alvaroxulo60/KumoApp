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
        // Configuración para que el Enter en los campos de texto dispare el login
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
                    .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(pass))
                    .findFirst()
                    .orElse(null);

            if (encontrado != null) {
                Sesion.setUsuario(encontrado);
                App.setRoot("MainView");
            } else {
                mostrarAlerta("Error de acceso", "Email o contraseña incorrectos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de conexión", "Error al conectar con la base de datos.");
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