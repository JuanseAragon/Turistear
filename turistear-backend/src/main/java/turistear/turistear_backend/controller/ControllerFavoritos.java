package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.service.ServiceFavoritos;

import java.util.Set;

@RestController
@RequestMapping("/api/favoritos")
public class ControllerFavoritos {

    private final ServiceFavoritos serviceFavoritos;

    public ControllerFavoritos(ServiceFavoritos serviceFavoritos) {
        this.serviceFavoritos = serviceFavoritos;
    }


    @PostMapping("/{idUsuario}/{idItinerario}")
    public void agregarAFavoritos(
            @PathVariable Long idUsuario,
            @PathVariable Long idItinerario) {
        serviceFavoritos.agregarAFavoritos(idUsuario, idItinerario);
    }

    // DELETE /api/favoritos/{idUsuario}/{idItinerario}
    @DeleteMapping("/{idUsuario}/{idItinerario}")
    public void eliminarDeFavoritos(
            @PathVariable Long idUsuario,
            @PathVariable Long idItinerario) {
        serviceFavoritos.eliminarDeFavoritos(idUsuario, idItinerario);
    }

    // GET /api/favoritos/{idUsuario}
    @GetMapping("/{idUsuario}")
    public Set<ItinerarioDTO> obtenerFavoritos(@PathVariable Long idUsuario) {
        return serviceFavoritos.obtenerFavoritos(idUsuario);
    }
}
