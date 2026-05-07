package DAO;

import exception.AppException;
import interfacesDAO.GeneroDAO;
import interfacesDAO.PlataformaDAO;
import interfacesDAO.VideojuegoDAO;
import io.ConexionDB;
import models.Genero;
import models.Plataforma;
import models.Videojuego;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideojuegoDAOMysql implements VideojuegoDAO {

    private final GeneroDAO generoDAO = new GeneroDAOMysql();
    private final PlataformaDAO plataformaDAO = new PlataformaDAOMysql();

    @Override
    public void insertar(Videojuego videojuego) throws AppException {
        String sqlInsertVideojuego = "INSERT INTO videojuego (titulo, desarrollador, año_lanzamiento) VALUES (?, ?, ?)";
        String sqlInsertPlataformas = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlInsertGeneros = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";
        String sqlInsertUsuVid = "INSERT INTO usuario_videojuego (Id_usuario, Id_videojuego, nota_personal, estado) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionDB.getInstance()) {
            con.setAutoCommit(false);

            try (PreparedStatement psJuego = con.prepareStatement(sqlInsertVideojuego, Statement.RETURN_GENERATED_KEYS)) {
                psJuego.setString(1, videojuego.getTitulo());
                psJuego.setString(2, videojuego.getDesarrollador());
                psJuego.setInt(3, videojuego.getAñoLanzamiento());
                psJuego.executeUpdate();

                int idGenerado = -1;
                try (ResultSet rs = psJuego.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID asignado al videojuego.");
                    }
                }
                videojuego.setIdVideojuego(idGenerado);

                if (videojuego.getPlataformas() != null && !videojuego.getPlataformas().isEmpty()) {
                    try (PreparedStatement psPlat = con.prepareStatement(sqlInsertPlataformas)) {
                        for (Plataforma p : videojuego.getPlataformas()) {
                            psPlat.setInt(1, idGenerado);
                            psPlat.setInt(2, p.id());
                            psPlat.addBatch();
                        }
                        psPlat.executeBatch();
                    }
                }

                if (videojuego.getGeneros() != null && !videojuego.getGeneros().isEmpty()) {
                    try (PreparedStatement psGen = con.prepareStatement(sqlInsertGeneros)) {
                        for (Genero g : videojuego.getGeneros()) {
                            psGen.setInt(1, idGenerado);
                            psGen.setInt(2, g.id());
                            psGen.addBatch();
                        }
                        psGen.executeBatch();
                    }
                }

                if (io.Sesion.getUsuario() != null) {
                    try (PreparedStatement psUV = con.prepareStatement(sqlInsertUsuVid)) {
                        psUV.setInt(1, io.Sesion.getUsuario().getIdUsuario());
                        psUV.setInt(2, idGenerado);
                        psUV.setString(3, videojuego.getNotaPersonal()); // Se manda a la base de datos como String
                        psUV.setString(4, videojuego.getEstado());
                        psUV.executeUpdate();
                    }
                }

                con.commit();
                System.out.println("✅ Videojuego insertado con éxito.");

            } catch (SQLException e) {
                con.rollback();
                throw new AppException("Error al insertar el videojuego y sus relaciones: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error en la conexión o transacción de inserción: " + e.getMessage());
        }
    }

    @Override
    public List<Videojuego> listarPorUsuario(int idUsuario) throws AppException {
        List<Videojuego> juegos = new ArrayList<>();
        String sql = "SELECT v.*, uv.nota_personal, uv.estado FROM videojuego v " +
                "JOIN usuario_videojuego uv ON v.Id_videojuego = uv.Id_videojuego " +
                "WHERE uv.Id_usuario = ?";

        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Videojuego v = mapearVideojuegoBasico(rs);

                    // Se lee de la base de datos como String
                    v.setNotaPersonal(rs.getString("nota_personal"));
                    v.setEstado(rs.getString("estado"));

                    v.setGeneros(obtenerGenerosDeJuego(v.getIdVideojuego()));
                    v.setPlataformas(obtenerPlataformasDeJuego(v.getIdVideojuego()));
                    juegos.add(v);
                }
            }
        } catch (SQLException e) {
            throw new AppException("Error al listar juegos del usuario: " + e.getMessage());
        }
        return juegos;
    }

    @Override
    public void actualizarVideojuego(Videojuego videojuego) {
        String sqlUpdateBase = "UPDATE videojuego SET titulo = ?, desarrollador = ?, año_lanzamiento = ? WHERE Id_videojuego = ?";
        String sqlDeletePlat = "DELETE FROM videojuego_plataforma WHERE Id_videojuego = ?";
        String sqlInsertPlat = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlDeleteGen = "DELETE FROM videojuego_genero WHERE Id_videojuego = ?";
        String sqlInsertGen = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";
        String sqlUpdateUsuVid = "UPDATE usuario_videojuego SET nota_personal = ?, estado = ? WHERE Id_usuario = ? AND Id_videojuego = ?";

        try (Connection con = ConexionDB.getInstance()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateBase)) {
                    ps.setString(1, videojuego.getTitulo());
                    ps.setString(2, videojuego.getDesarrollador());
                    ps.setInt(3, videojuego.getAñoLanzamiento());
                    ps.setInt(4, videojuego.getIdVideojuego());
                    ps.executeUpdate();
                }

                try (PreparedStatement psDelP = con.prepareStatement(sqlDeletePlat)) {
                    psDelP.setInt(1, videojuego.getIdVideojuego());
                    psDelP.executeUpdate();
                }
                if (videojuego.getPlataformas() != null) {
                    try (PreparedStatement psInsP = con.prepareStatement(sqlInsertPlat)) {
                        for (Plataforma p : videojuego.getPlataformas()) {
                            psInsP.setInt(1, videojuego.getIdVideojuego());
                            psInsP.setInt(2, p.id());
                            psInsP.addBatch();
                        }
                        psInsP.executeBatch();
                    }
                }

                try (PreparedStatement psDelG = con.prepareStatement(sqlDeleteGen)) {
                    psDelG.setInt(1, videojuego.getIdVideojuego());
                    psDelG.executeUpdate();
                }
                if (videojuego.getGeneros() != null) {
                    try (PreparedStatement psInsG = con.prepareStatement(sqlInsertGen)) {
                        for (Genero g : videojuego.getGeneros()) {
                            psInsG.setInt(1, videojuego.getIdVideojuego());
                            psInsG.setInt(2, g.id());
                            psInsG.addBatch();
                        }
                        psInsG.executeBatch();
                    }
                }

                if (io.Sesion.getUsuario() != null) {
                    try (PreparedStatement psUpdUV = con.prepareStatement(sqlUpdateUsuVid)) {
                        psUpdUV.setString(1, videojuego.getNotaPersonal()); // Se manda a la base de datos como String
                        psUpdUV.setString(2, videojuego.getEstado());
                        psUpdUV.setInt(3, io.Sesion.getUsuario().getIdUsuario());
                        psUpdUV.setInt(4, videojuego.getIdVideojuego());
                        psUpdUV.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new AppException("Error al actualizar las listas del videojuego: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error de conexión o transacción: " + e.getMessage());
        }
    }

    @Override
    public void eliminarVideojuego(int id) throws AppException {
        String sqlVideojuego = "DELETE FROM videojuego WHERE Id_videojuego = ?";
        try(Connection c = ConexionDB.getInstance();
            PreparedStatement pr4 = c.prepareStatement(sqlVideojuego)) {

            pr4.setInt(1, id);
            pr4.executeUpdate();

        } catch (SQLException e) {
            throw new AppException("Error: " + e.getMessage());
        }
    }

    @Override
    public Videojuego videojuegoObtenerPorID(int id) throws AppException, SQLException {
        return null;
    }

    @Override
    public List<Videojuego> listarTodos() throws AppException {
        return new ArrayList<>();
    }

    private List<Genero> obtenerGenerosDeJuego(int idJuego) throws SQLException, AppException {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT g.* FROM genero g JOIN videojuego_genero vg ON g.Id_genero = vg.Id_genero WHERE vg.Id_videojuego = ?";
        try (Connection con = ConexionDB.getInstance(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Genero(rs.getInt("Id_genero"), rs.getString("nombre_genero")));
            }
        }
        return lista;
    }

    private List<Plataforma> obtenerPlataformasDeJuego(int idJuego) throws SQLException, AppException {
        List<Plataforma> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM plataformas p JOIN videojuego_plataforma vp ON p.Id_plataforma = vp.Id_plataforma WHERE vp.Id_videojuego = ?";
        try (Connection con = ConexionDB.getInstance(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Plataforma(rs.getInt("Id_plataforma"), rs.getString("nombre_plataforma")));
            }
        }
        return lista;
    }

    private Videojuego mapearVideojuegoBasico(ResultSet rs) throws SQLException {
        Videojuego v = new Videojuego();
        v.setIdVideojuego(rs.getInt("Id_videojuego"));
        v.setTitulo(rs.getString("titulo"));
        v.setDesarrollador(rs.getString("desarrollador"));
        v.setAñoLanzamiento(rs.getInt("año_lanzamiento"));
        return v;
    }

    @Override
    public List<Genero> obtenerTodosLosGeneros() throws AppException, SQLException {
        return new ArrayList<>();
    }

    @Override
    public List<Plataforma> obtenerTodasLasPlataformas() throws AppException, SQLException {
        return new ArrayList<>();
    }
}