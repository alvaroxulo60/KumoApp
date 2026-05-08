package io;

import DAO.UsuarioDAOMysql;
import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class MigracionPasswords {

    public static void main(String[] args) {
        UsuarioDAOMysql usuarioDAO = new UsuarioDAOMysql();

        System.out.println("--- Iniciando migración de contraseñas ---");

        try {
            // 1. Obtener todos los usuarios de la base de datos
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            int cont = 0;

            for (Usuario u : usuarios) {
                String passActual = u.getPassword();

                // 2. Verificar si ya está hasheada para evitar doble cifrado
                // Los hashes de BCrypt suelen empezar por "$2a$" o "$2y$"
                if (passActual.startsWith("$2a$") || passActual.startsWith("$2y$")) {
                    System.out.println("Skipping: El usuario " + u.getNombre() + " ya tiene hash.");
                    continue;
                }

                // 3. Generar el nuevo hash
                String nuevoHash = BCrypt.hashpw(passActual, BCrypt.gensalt());
                u.setPassword(nuevoHash);

                // 4. Actualizar el usuario en la base de datos
                usuarioDAO.actualizar(u);

                System.out.println("OK: Contraseña migrada para el usuario: " + u.getNombre());
                cont++;
            }

            System.out.println("--- Migración finalizada con éxito ---");
            System.out.println("Total de usuarios actualizados: " + cont);

        } catch (Exception e) {
            System.err.println("ERROR durante la migración: " + e.getMessage());
            e.printStackTrace();
        }
    }
}