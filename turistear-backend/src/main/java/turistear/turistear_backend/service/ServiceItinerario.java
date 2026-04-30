package turistear.turistear_backend.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.dto.ItinerarioRequest;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceItinerario {

    private final UsuarioRepository repositorioUsuario;
    private final ItinerarioRepository repositorioItinerario;

    public ServiceItinerario(UsuarioRepository repositorioUsuario,
                             ItinerarioRepository itinerarioRepository) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioItinerario = itinerarioRepository;
    }


    // Correcto:
    @Transactional
    public ItinerarioDTO crearItinerario(ItinerarioRequest request) {
        // 1. Buscar el usuario REAL en la base
        Usuario creador = repositorioUsuario.findById(request.idCreador())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.idCreador()));

        // 2. Construir la entity
        Itinerario itinerario = new Itinerario();
        itinerario.setTitulo(request.titulo());
        itinerario.setDestino(request.destino());
        itinerario.setDescripcion(request.descripcion());
        itinerario.setEsPublico(Boolean.TRUE.equals(request.esPublico()));
        itinerario.setFechaInicio(request.fechaInicio());
        itinerario.setFechaFin(request.fechaFin());
        itinerario.setFechaCreacion(LocalDateTime.now()); // generado en el backend
        itinerario.setCreador(creador); // ← el usuario COMPLETO, no solo el id

        // 3. Guardar
        creador.getMis_itinerarios().add(itinerario);
        Itinerario guardado = repositorioItinerario.save(itinerario);
        
        return ItinerarioDTO.from(guardado);
    }

    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> obtenerItinerariosPorUsuario(Long idUsuario) {

        Usuario usuario = repositorioUsuario.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return usuario.getMis_itinerarios()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<ItinerarioDTO> getItinerariosFavoritos(Long id_usuario){
        Usuario usuario = repositorioUsuario.findById(id_usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return usuario.getFavoritos()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public ItinerarioDTO obtenerItinerarioPorId(Long idItinerario) {
        Itinerario itinerario = repositorioItinerario.findById(idItinerario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado"));
        return ItinerarioDTO.from(itinerario);
    }

    /**
     * Soft delete: marca el itinerario como eliminado en lugar de borrarlo
     * de la base. Las queries posteriores lo ignoran gracias al
     * {@code @SQLRestriction("eliminado = false")} en {@link Itinerario}.
     */
    @Transactional
    public void eliminarItinerario(Long idItinerario) {
        Itinerario itinerario = repositorioItinerario.findById(idItinerario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado"));
        itinerario.setEliminado(true);
        repositorioItinerario.save(itinerario);
    }

    @Transactional
    public ItinerarioDTO actualizarItinerario(Long idItinerario, RequestUpdateItinerary request) {
        Itinerario itinerario = repositorioItinerario.findById(idItinerario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado"));

        if (request.nombre() != null) {
            itinerario.setTitulo(request.nombre());
        }
        if (request.descripcion() != null) {
            itinerario.setDescripcion(request.descripcion());
        }
        if (request.esPublico() != null) {
            itinerario.setEsPublico(request.esPublico());
        }
        return ItinerarioDTO.from(repositorioItinerario.save(itinerario));
    }
}
