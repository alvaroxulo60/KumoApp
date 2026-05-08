// interfacesDAO/GeneroDAO.java
package interfacesDAO;
import models.Genero;
import exception.AppException;
import java.util.List;

public interface GeneroDAO {
    List<Genero> listarTodos() throws AppException;
}