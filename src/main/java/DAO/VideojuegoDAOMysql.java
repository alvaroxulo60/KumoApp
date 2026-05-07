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
        String sqlInsertVideojuego = "INSERT INTO videojuego (titulo, desarrollador, año_lanzamiento) VALUES (?, ?, ?)";
        String sqlInsertPlataformas = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlInsertGeneros = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";

        try (Connection con = ConexionDB.getInstance()) {
            con.setAutoCommit(false); // Iniciamos transacción para que todo sea atómico

            // 1. Insertar el videojuego y solicitar las llaves autogeneradas (ID)
            try (PreparedStatement psJuego = con.prepareStatement(sqlInsertVideojuego, Statement.RETURN_GENERATED_KEYS)) {
                psJuego.setString(1, videojuego.getTitulo());
                psJuego.setString(2, videojuego.getDesarrollador());
                psJuego.setInt(3, videojuego.getAñoLanzamiento());
                psJuego.executeUpdate();

                // Obtener el ID autogenerado
                int idGenerado = -1;
                try (ResultSet rs = psJuego.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID asignado al videojuego.");
                    }
                }

                // Asignamos el ID generado al objeto para que quede actualizado en memoria
                videojuego.setIdVideojuego(idGenerado);

                // 2. Insertar relaciones en la tabla intermedia de Plataformas
                if (videojuego.getPlataformas() != null && !videojuego.getPlataformas().isEmpty()) {
                    try (PreparedStatement psPlat = con.prepareStatement(sqlInsertPlataformas)) {
                        for (Plataforma p : videojuego.getPlataformas()) {
                            psPlat.setInt(1, idGenerado);
                            psPlat.setInt(2, p.id()); // .id() porque es un record
                            psPlat.addBatch(); // Procesamiento por lotes
                        }
                        psPlat.executeBatch();
                    }
                }

                // 3. Insertar relaciones en la tabla intermedia de Géneros
                if (videojuego.getGeneros() != null && !videojuego.getGeneros().isEmpty()) {
                    try (PreparedStatement psGen = con.prepareStatement(sqlInsertGeneros)) {
                        for (Genero g : videojuego.getGeneros()) {
                            psGen.setInt(1, idGenerado);
                            psGen.setInt(2, g.id()); // .id() porque es un record
                            psGen.addBatch();
                        }
                        psGen.executeBatch();
                    }
                }

                // Si todo se ejecutó correctamente, consolidamos los datos en la BD
                con.commit();
                System.out.println("✅ Videojuego insertado con éxito con el ID: " + idGenerado);

            } catch (SQLException e) {
                con.rollback(); // Deshace cualquier inserción a medias si algo falla
                throw new AppException("Error al insertar el videojuego y sus relaciones: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error en la conexión o transacción de inserción: " + e.getMessage());
        }
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
        // 1. Definimos todas las consultas necesarias
        String sqlUpdateBase = "UPDATE videojuego SET titulo = ?, desarrollador = ?, año_lanzamiento = ? WHERE Id_videojuego = ?";
        String sqlDeletePlat = "DELETE FROM videojuego_plataforma WHERE Id_videojuego = ?";
        String sqlInsertPlat = "INSERT INTO videojuego_plataforma (Id_videojuego, Id_plataforma) VALUES (?, ?)";
        String sqlDeleteGen = "DELETE FROM videojuego_genero WHERE Id_videojuego = ?";
        String sqlInsertGen = "INSERT INTO videojuego_genero (Id_videojuego, Id_genero) VALUES (?, ?)";

        try (Connection con = ConexionDB.getInstance()) {
            // MUY IMPORTANTE: Desactivar el autocommit para manejar la transacción manualmente
            con.setAutoCommit(false);

            try {
                // A. Actualizar datos básicos en la tabla principal
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateBase)) {
                    ps.setString(1, videojuego.getTitulo());
                    ps.setString(2, videojuego.getDesarrollador());
                    ps.setInt(3, videojuego.getAñoLanzamiento());
                    ps.setInt(4, videojuego.getIdVideojuego());
                    ps.executeUpdate();
                }

                // B. Sincronizar Lista de Plataformas
                // Primero borramos las que existían para este juego
                try (PreparedStatement psDelP = con.prepareStatement(sqlDeletePlat)) {
                    psDelP.setInt(1, videojuego.getIdVideojuego());
                    psDelP.executeUpdate();
                }
                // Luego insertamos la lista nueva
                if (videojuego.getPlataformas() != null) {
                    try (PreparedStatement psInsP = con.prepareStatement(sqlInsertPlat)) {
                        for (Plataforma p : videojuego.getPlataformas()) {
                            psInsP.setInt(1, videojuego.getIdVideojuego());
                            psInsP.setInt(2, p.id()); // Usamos .id() porque Plataforma es un record
                            psInsP.addBatch(); // Preparamos para ejecución en lote
                        }
                        psInsP.executeBatch();
                    }
                }

                // C. Sincronizar Lista de Géneros
                // Borramos los géneros antiguos
                try (PreparedStatement psDelG = con.prepareStatement(sqlDeleteGen)) {
                    psDelG.setInt(1, videojuego.getIdVideojuego());
                    psDelG.executeUpdate();
                }
                // Insertamos la lista nueva
                if (videojuego.getGeneros() != null) {
                    try (PreparedStatement psInsG = con.prepareStatement(sqlInsertGen)) {
                        for (Genero g : videojuego.getGeneros()) {
                            psInsG.setInt(1, videojuego.getIdVideojuego());
                            psInsG.setInt(2, g.id()); // Usamos .id() porque Genero es un record
                            psInsG.addBatch();
                        }
                        psInsG.executeBatch();
                    }
                }

                // Si todo ha ido bien, consolidamos los cambios en la DB
                con.commit();

            } catch (SQLException e) {
                // Si hay CUALQUIER error en las listas o el juego, deshacemos todo
                con.rollback();
                throw new AppException("Error al actualizar las listas del videojuego: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error de conexión o transacción: " + e.getMessage());
        }
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

    @Override
    public List<Videojuego> listarPorUsuario(int idUsuario) {
        List<Videojuego> juegos = new ArrayList<>();
        // Ajusta los nombres de las tablas según tu base de datos
        // Suponiendo una tabla intermedia 'usuario_videojuego'
        String sql = "SELECT v.*, " +
                "GROUP_CONCAT(DISTINCT g.nombre SEPARATOR ';') AS generos, " +
                "GROUP_CONCAT(DISTINCT p.nombre SEPARATOR ';') AS plataformas " +
                "FROM videojuego v " +
                "JOIN usuario_videojuego uv ON v.Id_videojuego = uv.Id_videojuego " +
                "LEFT JOIN videojuego_genero vg ON v.Id_videojuego = vg.Id_videojuego " +
                "LEFT JOIN genero g ON vg.Id_genero = g.id " +
                "LEFT JOIN videojuego_plataforma vp ON v.Id_videojuego = vp.Id_videojuego " +
                "LEFT JOIN plataforma p ON vp.Id_plataforma = p.id " +
                "WHERE uv.Id_usuario = ? " +
                "GROUP BY v.Id_videojuego";

        try (Connection con = ConexionDB.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    juegos.add(mapearVideojuego(rs)); // Reutiliza tu método mapear
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return juegos;
    }
}

