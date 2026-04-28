package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.service.ServiceComunidad;

import java.util.Set;

@RestController
@RequestMapping("/api/comunidad")
public class ControllerComunidad {

    private final ServiceComunidad serviceComunidad;

    public ControllerComunidad(ServiceComunidad serviceComunidad) {
        this.serviceComunidad = serviceComunidad;
    }

    // GET /api/comunidad/publicaciones
    @GetMapping("/publicaciones")
    public Set<ItinerarioDTO> listarPublicaciones() {
        return serviceComunidad.listarPublicaciones();
    }

    // GET /api/comunidad/ranking
    @GetMapping("/ranking")
    public Set<ItinerarioDTO> obtenerRanking() {
        return serviceComunidad.obtenerRanking();
    }

    // POST /api/comunidad/publicar/{idItinerario}/{idUsuario}
    @PostMapping("/publicar/{idItinerario}/{idUsuario}")
    public void publicarItinerario(
            @PathVariable Long idItinerario,
            @PathVariable Long idUsuario) {
        serviceComunidad.publicarItinerario(idItinerario, idUsuario);
    }

    // DELETE /api/comunidad/publicaciones/{idItinerario}/{idUsuario}
    @DeleteMapping("/publicaciones/{idItinerario}/{idUsuario}")
    public void eliminarPublicacion(
            @PathVariable Long idItinerario,
            @PathVariable Long idUsuario) {
        serviceComunidad.eliminarPublicacion(idItinerario, idUsuario);
    }

    // GET /api/comunidad/publicaciones/{idItinerario}
    @GetMapping("/publicaciones/{idItinerario}")
    public ItinerarioDTO obtenerPublicacionPorId(@PathVariable Long idItinerario) {
        return serviceComunidad.obtenerPublicacionPorId(idItinerario);
    }
}
