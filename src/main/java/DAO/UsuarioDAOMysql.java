package DAO;

import interfacesDAO.UsuarioDAO;
import io.ConexionDB;
import models.Usuario;
import exception.AppException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOMysql implements UsuarioDAO {

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuario";
        List<Usuario> listaUsuarios = new ArrayList<>();

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaUsuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException | AppException e) {
            System.err.println("Error al listar los usuarios: " + e.getMessage());
        }
        return listaUsuarios;
    }

    @Override
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE Id_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException | AppException e) {
            System.err.println("Error al obtener el usuario por ID: " + e.getMessage());
        }
        return usuario;
    }

    @Override
    public void insertar(Usuario u) {
        String sql = "INSERT INTO usuario (username, Email, teléfono, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getNumeroTelefono());
            ps.setString(4, u.getPassword());

            ps.executeUpdate();
            System.out.println("✅ Usuario registrado con éxito.");
        } catch (SQLException | AppException e) {
            System.err.println("Error al intentar insertar el usuario: " + e.getMessage());
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET username = ?, Email = ?, teléfono = ?, password = ? WHERE Id_usuario = ?";

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getNumeroTelefono());
            ps.setString(4, usuario.getPassword());
            ps.setInt(5, usuario.getIdUsuario());

            ps.executeUpdate();
            System.out.println("✅ Usuario actualizado correctamente.");
        } catch (SQLException | AppException e) {
            System.err.println("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE Id_usuario = ?";

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Usuario eliminado.");
        } catch (SQLException | AppException e) {
            System.err.println("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("Id_usuario"));
        usuario.setNombre(rs.getString("username"));
        usuario.setEmail(rs.getString("Email"));
        usuario.setNumeroTelefono(rs.getString("teléfono"));
        usuario.setPassword(rs.getString("password"));
        usuario.setAdmin(rs.getBoolean("es_admin")); // Aquí obtenemos si es admin de la BD
        return usuario;
    }
}