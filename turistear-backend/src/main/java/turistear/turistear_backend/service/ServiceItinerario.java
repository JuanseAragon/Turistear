package turistear.turistear_backend.service;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.dto.ItinerarioRequest;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + request.idCreador()));

        // 2. Construir la entity
        Itinerario itinerario = new Itinerario();
        itinerario.setTitulo(request.titulo());
        itinerario.setDestino(request.destino());
        itinerario.setDescripcion(request.descripcion());
        itinerario.setEsPublico(request.esPublico());
        itinerario.setFechaInicio(request.fechaInicio());
        itinerario.setFechaFin(request.fechaFin());
        itinerario.setFechaCreacion(LocalDateTime.now()); // generado en el backend
        itinerario.setCreador(creador); // ← el usuario COMPLETO, no solo el id

        // 3. Guardar
        creador.getMis_itinerarios().add(itinerario);
        Itinerario guardado = repositorioItinerario.save(itinerario);
        
        return ItinerarioDTO.from(guardado);
    }

    public Set<ItinerarioDTO> obtenerItinerariosPorUsuario(Long idUsuario) {

        Usuario usuario = repositorioUsuario.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuario.getMis_itinerarios()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }

    public Set<ItinerarioDTO> getItinerariosFavoritos(Long id_usuario){
        Usuario usuario = repositorioUsuario.findById(id_usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return usuario.getFavoritos()
                .stream()
                .map(ItinerarioDTO::from)
                .collect(Collectors.toSet());
    }
}
