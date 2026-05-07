package DAO;

import interfacesDAO.UsuarioDAO;
import io.ConexionDB;
import models.Usuario;
import exception.AppException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UsuarioDAOMysql implements UsuarioDAO {

    @Override
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE idUsuario = ?";
        Usuario usuario = null;

        try {
            Connection conn = ConexionDB.getInstance();
            try (PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, id);
                try (java.sql.ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        usuario = new Usuario();
                        usuario.setIdUsuario(rs.getInt("idUsuario"));
                        usuario.setNombre(rs.getString("nombre"));
                        usuario.setEmail(rs.getString("email"));
                        usuario.setNumeroTelefono(rs.getInt("numeroTelefono"));
                        usuario.setPassword(rs.getString("password"));
                    }
                }
            }
        }catch (SQLException | AppException e){
            System.err.println("Error al obtener el usuario por ID: " + e.getMessage());
        }
        return  usuario;
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuario";
        List<Usuario> listaUsuarios = new java.util.ArrayList<>();

        try {
            Connection conn = ConexionDB.getInstance();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()){

                while (rs.next()){
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("idUsuario"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setPassword(rs.getString("password"));
                    usuario.setNumeroTelefono(rs.getInt("numeroTelefono"));
                }
            }
        }catch (SQLException | AppException e){
            System.err.println("Error al listar los usuarios: " + e.getMessage());
        }
        return listaUsuarios;
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, numeroTelefono = ?, password = ? WHERE idUsuario = ?";

        try {
            Connection conn = ConexionDB.getInstance();
            try (PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getEmail());
                ps.setInt(3, usuario.getNumeroTelefono());
                ps.setString(4, usuario.getPassword());
                ps.setInt(5, usuario.getIdUsuario());

                ps.executeUpdate();
            }
        }catch (SQLException | AppException e){
            System.err.println("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";

        try {
            Connection conn = ConexionDB.getInstance();
            try (PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }catch (SQLException | AppException e){
            System.err.println("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    @Override
    public void insertar(Usuario u) {

        String sql = "INSERT INTO usuario (idUsuario, nombre, email, numeroTelefono, password) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = ConexionDB.getInstance();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, u.getIdUsuario());
                ps.setString(2, u.getNombre());
                ps.setString(3, u.getEmail());
                ps.setInt(4, u.getNumeroTelefono());
                ps.setString(5, u.getPassword());

                ps.executeUpdate();
            }
        } catch (SQLException | AppException e) {

            System.err.println("Error al intentar insertar el usuario: " + e.getMessage());
        }
    }
}