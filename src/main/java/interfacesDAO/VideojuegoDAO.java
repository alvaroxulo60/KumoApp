package interfacesDAO;

import exception.AppException;
import models.Videojuego;

import java.sql.SQLException;
import java.util.List;

public interface VideojuegoDAO {
    void insertar(Videojuego videojuego) throws AppException;
    Videojuego videojuegoObtenerPorID(int id) throws AppException, SQLException;
    List<Videojuego> listarTodos() throws AppException;
    void actualizarVideojuego(Videojuego videojuego) throws AppException;
    void eliminarVideojuego(int id) throws AppException;
    List<Videojuego> listarPorUsuario(int idUsuario) throws AppException;
}
