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

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim(); // .trim() quita espacios vacíos
        String pass = txtPassword.getText();

        UsuarioDAOMysql userDAO = new UsuarioDAOMysql();
        try {
            List<Usuario> usuarios = userDAO.listarTodos();

            for (Usuario u: usuarios){
                System.out.println(u.getEmail() + " - " + u.getPassword());
            }

            // Buscamos si existe el usuario con ignoreCase para el email
            Usuario encontrado = usuarios.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(pass))
                    .findFirst()
                    .orElse(null);

            if (encontrado != null) {
                Sesion.setUsuario(encontrado);
                App.setRoot("MainView");
            } else {
                mostrarAlerta("Error", "Credenciales incorrectas. Revisa el email y la contraseña.%s-%s".formatted(email, pass));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de conexión", "No se pudo conectar a MySQL.");
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