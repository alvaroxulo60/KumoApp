package interfacesDAO;
import models.Plataforma;
import exception.AppException;
import java.util.List;

public interface PlataformaDAO {
    List<Plataforma> listarTodas() throws AppException;
}