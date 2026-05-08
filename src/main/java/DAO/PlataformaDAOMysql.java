package DAO;
import interfacesDAO.PlataformaDAO;
import io.ConexionDB;
import models.Plataforma;
import exception.AppException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlataformaDAOMysql implements PlataformaDAO {
    @Override
    public List<Plataforma> listarTodas() throws AppException {
        List<Plataforma> lista = new ArrayList<>();
        String sql = "SELECT Id_plataforma, nombre_plataforma FROM plataformas";
        try (Connection con = ConexionDB.getInstance();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Plataforma(rs.getInt("Id_plataforma"), rs.getString("nombre_plataforma")));
            }
        } catch (SQLException e) {
            throw new AppException("Error al listar plataformas: " + e.getMessage());
        }
        return lista;
    }
}