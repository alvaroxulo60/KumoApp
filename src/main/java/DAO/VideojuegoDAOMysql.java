package DAO;

import exception.AppException;
import interfacesDAO.VideojuegoDAO;
import io.ConexionDB;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideojuegoDAOMysql implements VideojuegoDAO {


    @Override
    public void insertar(Videojuego videojuego) throws AppException {

    }


    @Override
    public Videojuego videojuegoObtenerPorID(int id) throws AppException, SQLException {
        String sql = "SELECT \n" +
                "    v.*, \n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(g.Id_genero, ':', g.nombre_genero) SEPARATOR ';') AS datos_generos,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(p.Id_plataforma, ':', p.nombre_plataforma) SEPARATOR ';') AS datos_plataformas\n" +
                "FROM videojuego v\n" +
                "JOIN videojuego_genero vg ON v.Id_videojuego = vg.Id_videojuego\n" +
                "JOIN genero g ON vg.Id_genero = g.Id_genero\n" +
                "JOIN videojuego_plataforma vp ON v.Id_videojuego = vp.Id_videojuego\n" +
                "JOIN plataformas p ON vp.Id_plataforma = p.Id_plataforma\n" +
                "WHERE v.Id_videojuego = ?\n" +
                "GROUP BY v.Id_videojuego;";

        try (Connection con = ConexionDB.getInstance();
             PreparedStatement pr1 = con.prepareStatement(sql)) {

            pr1.setInt(1, id);

            try (ResultSet rs = pr1.executeQuery()) {
                if (rs.next()) {
                    return mapearVideojuego(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Videojuego> listarTodos() throws AppException {
        String sql = "SELECT \n" +
                "    v.*, \n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(g.Id_genero, ':', g.nombre_genero) SEPARATOR ';') AS datos_generos,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(p.Id_plataforma, ':', p.nombre_plataforma) SEPARATOR ';') AS datos_plataformas\n" +
                "FROM videojuego v\n" +
                "JOIN videojuego_genero vg ON v.Id_videojuego = vg.Id_videojuego\n" +
                "JOIN genero g ON vg.Id_genero = g.Id_genero\n" +
                "JOIN videojuego_plataforma vp ON v.Id_videojuego = vp.Id_videojuego\n" +
                "JOIN plataformas p ON vp.Id_plataforma = p.Id_plataforma\n" +
                "GROUP BY v.Id_videojuego;";

        List<Videojuego> videojuegos = new ArrayList<>();

        try (Connection connection = ConexionDB.getInstance();
             Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                videojuegos.add(mapearVideojuego(rs));
            }


        } catch (SQLException e) {
            throw new AppException("Error: "+e.getMessage());
        }
        return videojuegos;
    }

    /**
     * Metodo para crear los videojuegos a partir de un rs
     * @param rs el Result set de la consulta
     * @return cada videojuego creado
     * @throws SQLException
     */
    private Videojuego mapearVideojuego(ResultSet rs) throws SQLException {
        Videojuego v = new Videojuego();

        v.setIdVideojuego(rs.getInt("Id_videojuego"));
        v.setTitulo(rs.getString("titulo"));
        v.setDesarrollador(rs.getString("desarrollador"));
        v.setAñoLanzamiento(rs.getInt("año_lanzamiento"));

        // Procesar Géneros
        String generos = rs.getString("datos_generos");
        if (generos != null && !generos.isEmpty()) {
            List<Genero> listG = new ArrayList<>();
            for (String items : generos.split(";")) {
                String[] partes = items.split(":");
                if (partes.length == 2) {
                    listG.add(new Genero(Integer.parseInt(partes[0]), partes[1]));
                }
            }
            v.setGeneros(listG);
        }

        // Procesar Plataformas
        String plataformas = rs.getString("datos_plataformas");
        if (plataformas != null && !plataformas.isEmpty()) {
            List<Plataforma> listP = new ArrayList<>();
            for (String item : plataformas.split(";")) {
                String[] partes = item.split(":");
                if (partes.length == 2) {
                    listP.add(new Plataforma(Integer.parseInt(partes[0]), partes[1]));
                }
            }
            v.setPlataformas(listP);
        }

        return v;
    }


    @Override
    public void actualizarVideojuego(Videojuego videojuego) {

    }

    @Override
    public void eliminarVideojuego(int id) throws AppException {
        // Delete de las relaciones
        String sqlPlataforma = "Delete from videojuego_plataforma where Id_videojuego = ?";
        String sqlUsuario = "Delete from videojuego_genero where Id_videojuego = ?";
        String sqlGenero = "Delete from usuario_videojuego where Id_videojuego = ?";

        //delete de la tabla videojuego
        String sqlVideojuego = "Delete from videojuego where Id_videojuego = ?";

        try(Connection c = ConexionDB.getInstance();
        PreparedStatement pr1 = c.prepareStatement(sqlPlataforma);
        PreparedStatement pr2 = c.prepareStatement(sqlUsuario);
        PreparedStatement pr3 = c.prepareStatement(sqlGenero);
        PreparedStatement pr4 = c.prepareStatement(sqlVideojuego)){
            c.setAutoCommit(false);

            pr1.setInt(1,id);
            pr2.setInt(1,id);
            pr3.setInt(1,id);
            pr4.setInt(1,id);

            pr1.executeUpdate();
            pr2.executeUpdate();
            pr3.executeUpdate();
            int linesAfectadas = pr4.executeUpdate();

            if (linesAfectadas == 1){
                c.commit();
            }else {
                c.rollback();
                throw new AppException("No se ha podido borrar el juego");
            }

        } catch (SQLException e) {
            throw new AppException("Error: " + e.getMessage());
        }
    }

    public List<Genero> obtenerTodosLosGeneros() throws AppException, SQLException {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT Id_genero, nombre FROM generos"; // Ajusta según tus columnas reales

        try (Connection con = ConexionDB.getInstance(); // Usa tu método de conexión
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Genero g = new Genero(rs.getInt("id"),rs.getString("nombre"));
                lista.add(g);
            }
        }
        return lista;
    }

    public List<Plataforma> obtenerTodasLasPlataformas() throws AppException, SQLException {
        List<Plataforma> lista = new ArrayList<>();
        String sql = "SELECT Id_plataforma, nombre FROM plataformas"; // Ajusta según tus columnas reales

        try (Connection con = ConexionDB.getInstance();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Plataforma p = new Plataforma(rs.getInt("id"), rs.getString("nombre"));
                lista.add(p);
            }
        }
        return lista;
    }
}

