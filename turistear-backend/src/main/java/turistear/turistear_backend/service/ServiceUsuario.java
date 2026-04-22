package turistear.turistear_backend.service;

import org.springframework.stereotype.Service;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

@Service
public class ServiceUsuario {

    private final UsuarioRepository repositorioUsuario;
    private final ItinerarioRepository repositorioItinerario;

    public ServiceUsuario(UsuarioRepository repositorioUsuario,
                          ItinerarioRepository itinerarioRepository) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioItinerario = itinerarioRepository;
    }

    public Usuario crearUsuario(Usuario usuario) {
        return repositorioUsuario.save(usuario);
    }

    public void eliminarUsuario(Long idUsuario){
        repositorioUsuario.deleteById(idUsuario);
    }
}
