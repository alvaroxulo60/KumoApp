package controller;

import DAO.UsuarioDAOMysql;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPassword;

    private final UsuarioDAOMysql usuarioDAO = new UsuarioDAOMysql();

    @FXML
    private void handleRegistrar() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String passwordPlana = txtPassword.getText();

        if (username.isEmpty() || email.isEmpty() || passwordPlana.isEmpty()) {
            mostrarAlerta("Campos vacíos", "El usuario, email y contraseña son campos obligatorios.");
            return;
        }

        // GENERAR HASH DE LA CONTRASEÑA
        String passwordHasheada = BCrypt.hashpw(passwordPlana, BCrypt.gensalt());

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setNumeroTelefono(telefono);
        nuevoUsuario.setPassword(passwordHasheada); // Guardamos el hash, no el texto plano

        try {
            usuarioDAO.insertar(nuevoUsuario);
            mostrarAlerta("Éxito", "¡Cuenta creada correctamente! Ya puedes iniciar sesión.");
            cerrar();
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un problema al registrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtUsername.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}