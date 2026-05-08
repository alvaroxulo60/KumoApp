package interfacesDAO;

import exception.AppException;
import models.Videojuego;

import java.sql.SQLException;
import java.util.List;

public interface VideojuegoDAO {
    // Métodos globales del catálogo (Admin)
    void insertar(Videojuego videojuego) throws AppException;
    Videojuego videojuegoObtenerPorID(int id) throws AppException, SQLException;
    List<Videojuego> listarTodos() throws AppException;
    void actualizarVideojuego(Videojuego videojuego) throws AppException;
    void eliminarVideojuego(int id) throws AppException;

    // Métodos de la biblioteca personal (Usuario)
    List<Videojuego> listarPorUsuario(int idUsuario) throws AppException;
    void eliminarJuegoDeUsuario(int idUsuario, int idVideojuego) throws AppException;
    void anadirJuegoColeccion(int idUsuario, int idVideojuego, String estado, String notaPersonal) throws AppException;
    void actualizarRelacionUsuarioJuego(int idUsuario, int idVideojuego, String estado, String notaPersonal) throws AppException;
}