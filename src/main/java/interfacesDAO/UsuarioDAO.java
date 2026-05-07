package interfacesDAO;

import models.Usuario;

import java.util.List;

public interface UsuarioDAO {
    void insertar(Usuario usuario);
    Usuario obtenerPorId(int id);
    List<Usuario> listarTodos();
    void actualizar(Usuario usuario);
    void eliminar(int id);
}

