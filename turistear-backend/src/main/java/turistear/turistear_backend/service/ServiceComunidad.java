package turistear.turistear_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.enumerable.CategoriaActividad;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceComunidad {

    private final ItinerarioRepository repositoryItinerario;
    private final UsuarioRepository repositoryUsuario;

    public ServiceComunidad(ItinerarioRepository repositoryItinerario,
                            UsuarioRepository repositoryUsuario) {
        this.repositoryItinerario = repositoryItinerario;
        this.repositoryUsuario = repositoryUsuario;
    }

    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> listarPublicaciones() {
        return repositoryItinerario.findByEsPublicoTrue()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> obtenerRanking() {
        return repositoryItinerario.rankingPublicaciones()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void publicarItinerario(Long idItinerario, Long idUsuario) {
        Itinerario itinerario = repositoryItinerario.findById(idItinerario)
                .orElseThrow(() -> new RuntimeException("Itinerario no encontrado"));

        // Validación: solo el creador puede publicar
        if (!itinerario.getCreador().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Solo el creador puede publicar este itinerario");
        }

        itinerario.setEsPublico(true);
        repositoryItinerario.save(itinerario);
    }

    @Transactional
    public void eliminarPublicacion(Long idItinerario, Long idUsuario) {
        Itinerario itinerario = repositoryItinerario.findById(idItinerario)
                .orElseThrow(() -> new RuntimeException("Itinerario no encontrado"));

        // Validación: solo el creador puede despublicar
        if (!itinerario.getCreador().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Solo el creador puede despublicar este itinerario");
        }

        // "Eliminar publicación" = despublicar. NO borramos el itinerario.
        itinerario.setEsPublico(false);
        repositoryItinerario.save(itinerario);
    }

    @Transactional(readOnly = true)
    public ItinerarioDTO obtenerPublicacionPorId(Long idItinerario) {
        Itinerario itinerario = repositoryItinerario.findById(idItinerario)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        if (!Boolean.TRUE.equals(itinerario.getEsPublico())) {
            throw new RuntimeException("Este itinerario no está publicado");
        }
        return ItinerarioDTO.from(itinerario);
    }

    /**
     * Búsqueda de publicaciones por provincia.
     * Filtra directamente sobre Itinerario.destino (enum Provincia).
     * Si provincia viene null, devuelve todas las publicaciones.
     */
    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> buscarPorRegion(Provincia provincia) {
        return repositoryItinerario.buscarPorRegion(provincia)
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    /**
     * Filtro de publicaciones por categoría/etiqueta (ej: NATURALEZA, GASTRONOMICO).
     * Devuelve los itinerarios públicos que contengan al menos una actividad
     * etiquetada con la categoría indicada.
     */
    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> buscarPorCategoria(CategoriaActividad categoria) {
        return repositoryItinerario.buscarPorCategoria(categoria)
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }
}
