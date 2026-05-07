package controller;

import DAO.UsuarioDAOMysql;
import io.App;
import io.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.Usuario;
import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    @FXML
    private void handleLogin() {
        UsuarioDAOMysql userDAO = new UsuarioDAOMysql();
        try {
            List<Usuario> usuarios = userDAO.listarTodos();
            Usuario encontrado = usuarios.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(txtEmail.getText())
                            && u.getPassword().equals(txtPassword.getText()))
                    .findFirst().orElse(null);

            if (encontrado != null) {
                Sesion.setUsuario(encontrado);
                App.setRoot("MainView");
            } else {
                mostrarAlerta("Error", "Credenciales incorrectas");
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