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
        String sqlInsertVideojuego = "INSERT INTO videojuego (titulo, desarrollador, año_lanzamiento, ruta_portada) VALUES (?, ?, ?, ?)";
        String sqlInsertPlataformas = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlInsertGeneros = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";

        try (Connection con = ConexionDB.getInstance()) {
            con.setAutoCommit(false);

            try (PreparedStatement psJuego = con.prepareStatement(sqlInsertVideojuego, Statement.RETURN_GENERATED_KEYS)) {
                psJuego.setString(1, videojuego.getTitulo());
                psJuego.setString(2, videojuego.getDesarrollador());
                psJuego.setInt(3, videojuego.getAñoLanzamiento());
                psJuego.setBytes(4, videojuego.getPortada());
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

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new AppException("Error al insertar el videojuego: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error en la conexión o transacción de inserción: " + e.getMessage());
        }
    }

    @Override
    public List<Videojuego> listarTodos() throws AppException {
        List<Videojuego> juegos = new ArrayList<>();
        String sql = "SELECT * FROM videojuego";
        try (Connection con = ConexionDB.getInstance();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Videojuego v = mapearVideojuegoBasico(rs);
                v.setGeneros(obtenerGenerosDeJuego(v.getIdVideojuego()));
                v.setPlataformas(obtenerPlataformasDeJuego(v.getIdVideojuego()));
                juegos.add(v);
            }
        } catch (SQLException e) {
            throw new AppException("Error al listar catálogo: " + e.getMessage());
        }
        return juegos;
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
    public void anadirJuegoColeccion(int idUsuario, int idVideojuego, String estado, String notaPersonal) throws AppException {
        String sql = "INSERT INTO usuario_videojuego (Id_usuario, Id_videojuego, estado, nota_personal) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setString(3, estado);
            ps.setString(4, notaPersonal);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Error al añadir a tu lista (Es posible que ya esté en tu biblioteca): " + e.getMessage());
        }
    }

    @Override
    public void actualizarRelacionUsuarioJuego(int idUsuario, int idVideojuego, String estado, String notaPersonal) throws AppException {
        String sql = "UPDATE usuario_videojuego SET estado = ?, nota_personal = ? WHERE Id_usuario = ? AND Id_videojuego = ?";
        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, notaPersonal);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idVideojuego);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Error al actualizar tu valoración: " + e.getMessage());
        }
    }

    @Override
    public void eliminarJuegoDeUsuario(int idUsuario, int idVideojuego) throws AppException {
        String sql = "DELETE FROM usuario_videojuego WHERE Id_usuario = ? AND Id_videojuego = ?";
        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new AppException("Error al eliminar el juego de tu colección: " + e.getMessage());
        }
    }

    @Override
    public void actualizarVideojuego(Videojuego videojuego) throws AppException {
        String sqlUpdateBase = "UPDATE videojuego SET titulo = ?, desarrollador = ?, año_lanzamiento = ?, ruta_portada = ? WHERE Id_videojuego = ?";
        String sqlDeletePlat = "DELETE FROM videojuego_plataforma WHERE Id_videojuego = ?";
        String sqlInsertPlat = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlDeleteGen = "DELETE FROM videojuego_genero WHERE Id_videojuego = ?";
        String sqlInsertGen = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";

        try (Connection con = ConexionDB.getInstance()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateBase)) {
                    ps.setString(1, videojuego.getTitulo());
                    ps.setString(2, videojuego.getDesarrollador());
                    ps.setInt(3, videojuego.getAñoLanzamiento());
                    ps.setBytes(4, videojuego.getPortada());
                    ps.setInt(5, videojuego.getIdVideojuego());
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

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new AppException("Error al actualizar el videojuego: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error de conexión o transacción: " + e.getMessage());
        }
    }

    @Override
    public void eliminarVideojuego(int id) throws AppException {
        String sqlDelGen = "DELETE FROM videojuego_genero WHERE Id_videojuego = ?";
        String sqlDelPlat = "DELETE FROM videojuego_plataforma WHERE Id_videojuego = ?";
        String sqlDelUV = "DELETE FROM usuario_videojuego WHERE Id_videojuego = ?";
        String sqlVideojuego = "DELETE FROM videojuego WHERE Id_videojuego = ?";

        try (Connection c = ConexionDB.getInstance()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p1 = c.prepareStatement(sqlDelGen)) { p1.setInt(1, id); p1.executeUpdate(); }
                try (PreparedStatement p2 = c.prepareStatement(sqlDelPlat)) { p2.setInt(1, id); p2.executeUpdate(); }
                try (PreparedStatement p3 = c.prepareStatement(sqlDelUV)) { p3.setInt(1, id); p3.executeUpdate(); }
                try (PreparedStatement p4 = c.prepareStatement(sqlVideojuego)) { p4.setInt(1, id); p4.executeUpdate(); }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw new AppException("Error al eliminar las dependencias del videojuego: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new AppException("Error de conexión al eliminar: " + e.getMessage());
        }
    }

    @Override
    public Videojuego videojuegoObtenerPorID(int id) throws AppException, SQLException {
        String sql = "SELECT * FROM videojuego WHERE Id_videojuego = ?";
        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Videojuego v = mapearVideojuegoBasico(rs);
                    v.setGeneros(obtenerGenerosDeJuego(v.getIdVideojuego()));
                    v.setPlataformas(obtenerPlataformasDeJuego(v.getIdVideojuego()));
                    return v;
                }
            }
        }
        return null;
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
        v.setPortada(rs.getBytes("ruta_portada"));
        return v;
    }
}