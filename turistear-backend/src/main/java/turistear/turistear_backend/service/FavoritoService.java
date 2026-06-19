package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.sistema.ItinerarioSistemaResumenDTO;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Favorito;
import turistear.turistear_backend.model.ItinerarioSistema;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.FavoritoRepository;
import turistear.turistear_backend.repository.ItinerarioSistemaRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.util.List;

/**
 * Lógica de favoritos como <strong>bookmarks</strong>: marcar un
 * itinerario del sistema como favorito sin crear copia. La copia
 * editable se crea aparte (ver el flujo de itinerarios de usuario).
 *
 * Mantiene el contador desnormalizado {@code likes} del sistema:
 * +1 al guardar, -1 al quitar. Es el mismo contador que ordena Explorar.
 */
@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepo;
    private final ItinerarioSistemaRepository sistemaRepo;
    private final UsuarioRepository usuarioRepo;

    /**
     * Guarda un template del sistema como favorito del usuario.
     * Idempotente: si ya lo tenía guardado no hace nada (no duplica ni
     * vuelve a sumar like). Si el template no existe, 404.
     */
    @Transactional
    public void guardarFavorito(Long idUsuario, Long idSistema) {
        if (favoritoRepo.existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(idUsuario, idSistema)) {
            return;
        }
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        ItinerarioSistema sistema = sistemaRepo.findById(idSistema)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Itinerario del sistema no encontrado: " + idSistema));

        favoritoRepo.save(Favorito.builder()
                .usuario(usuario)
                .itinerarioSistema(sistema)
                .build());
        sistemaRepo.incrementarLikes(idSistema);
    }

    /**
     * Lista los favoritos del usuario como resúmenes del template del
     * sistema (mismo DTO que usa Explorar). El frontend ya sabe renderizarlo.
     */
    @Transactional(readOnly = true)
    public List<ItinerarioSistemaResumenDTO> listarFavoritos(Long idUsuario) {
        return favoritoRepo.findByUsuarioConSistema(idUsuario).stream()
                .map(favorito -> ItinerarioSistemaResumenDTO.from(favorito.getItinerarioSistema()))
                .toList();
    }

    /**
     * Quita el favorito. Solo decrementa likes si efectivamente había un
     * bookmark que borrar (evita restar de más en llamadas repetidas).
     */
    @Transactional
    public void quitarFavorito(Long idUsuario, Long idSistema) {
        int favoritosBorrados = favoritoRepo.deleteByUsuarioAndSistema(idUsuario, idSistema);
        if (favoritosBorrados > 0) {
            sistemaRepo.decrementarLikes(idSistema);
        }
    }

    /** ¿El usuario ya guardó este template? Para el toggle del corazón en Explorar. */
    @Transactional(readOnly = true)
    public boolean existeFavorito(Long idUsuario, Long idSistema) {
        return favoritoRepo.existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(idUsuario, idSistema);
    }
}
