package turistear.turistear_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceFavoritos {

    private final UsuarioRepository repositoryUsuario;
    private final ItinerarioRepository repositoryItinerario;

    public ServiceFavoritos(UsuarioRepository repositoryUsuario,
                            ItinerarioRepository repositoryItinerario) {
        this.repositoryUsuario = repositoryUsuario;
        this.repositoryItinerario = repositoryItinerario;
    }

    @Transactional
    public void agregarAFavoritos(Long idUsuario, Long idItinerario) {
        Usuario usuario = repositoryUsuario.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Itinerario itinerario = repositoryItinerario.findById(idItinerario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado"));

        usuario.getFavoritos().add(itinerario);
        repositoryUsuario.save(usuario);
    }

    @Transactional
    public void eliminarDeFavoritos(Long idUsuario, Long idItinerario) {
        Usuario usuario = repositoryUsuario.findByIdConFavoritos(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.getFavoritos().removeIf(it -> it.getIdItinerario().equals(idItinerario));
        repositoryUsuario.save(usuario);
    }

    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> obtenerFavoritos(Long idUsuario) {
        Usuario usuario = repositoryUsuario.findByIdConFavoritos(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return usuario.getFavoritos()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }
}
