

// DAO/GeneroDAOMysql.java
package DAO;
import interfacesDAO.GeneroDAO;
import io.ConexionDB;
import models.Genero;
import exception.AppException;
import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class GeneroDAOMysql implements GeneroDAO {
    @Override
    public List<Genero> listarTodos() throws AppException {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT Id_genero, nombre_genero FROM genero";
        try (Connection con = ConexionDB.getInstance();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Genero(rs.getInt("Id_genero"), rs.getString("nombre_genero")));
            }
        } catch (SQLException e) {
            throw new AppException("Error al listar géneros: " + e.getMessage());
        }
        return lista;
    }
}