package controller;

import DAO.UsuarioDAOMysql;
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
        String email = txtEmail.getText();
        String pass = txtPassword.getText();

        UsuarioDAOMysql userDAO = new UsuarioDAOMysql();
        try {
            // Buscamos al usuario en la lista total
            List<Usuario> usuarios = userDAO.listarTodos();
            Usuario encontrado = usuarios.stream()
                    .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(pass))
                    .findFirst()
                    .orElse(null);

            if (encontrado != null) {
                Sesion.setUsuario(encontrado);
                System.out.println("Login correcto: " + encontrado.getNombre());
                // Aquí cargarías la siguiente escena (MainView.fxml)
            } else {
                mostrarAlerta("Error", "Credenciales inválidas");
            }
        } catch (Exception e) {
            mostrarAlerta("Error Crítico", e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}