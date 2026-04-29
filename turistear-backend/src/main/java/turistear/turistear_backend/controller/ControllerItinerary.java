package turistear.turistear_backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.dto.ItinerarioRequest;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.service.ServiceItinerario;


import java.util.Set;

@RestController
@RequestMapping("/itinerario")
public class ControllerItinerary {

    private final ServiceItinerario serviceItinerario;

    public ControllerItinerary(ServiceItinerario serviceItinerario){
        this.serviceItinerario = serviceItinerario;
    }

    @PostMapping
    public ResponseEntity<ItinerarioDTO> crearItinerario(@Valid @RequestBody ItinerarioRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(serviceItinerario.crearItinerario(request));
    }

    @GetMapping("/itinerarios")
    public Set<ItinerarioDTO> getItinerariosDeUsuario(@RequestParam Long id_usuario) {

        return serviceItinerario.obtenerItinerariosPorUsuario(id_usuario);
    }

    @GetMapping("/itinerarios/favoritos")
    public Set<ItinerarioDTO> getItinerariosFavoritos(@RequestParam Long id_usuario){
        return serviceItinerario.getItinerariosFavoritos(id_usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItinerarioDTO> obtenerItinerarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItinerario.obtenerItinerarioPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItinerarioDTO> actualizarItinerario(
            @PathVariable Long id,
            @Valid @RequestBody RequestUpdateItinerary request) {
        return ResponseEntity.ok(serviceItinerario.actualizarItinerario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItinerario(@PathVariable Long id) {
        serviceItinerario.eliminarItinerario(id);
        return ResponseEntity.noContent().build();
    }
}