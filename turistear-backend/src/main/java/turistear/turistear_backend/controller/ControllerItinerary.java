package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.dto.ItinerarioRequest;
import turistear.turistear_backend.model.Itinerario;
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
    public Itinerario crearItinerario(@RequestBody ItinerarioRequest request) {
        return serviceItinerario.crearItinerario(request);
    }

    @GetMapping("/itinerarios")
    public Set<ItinerarioDTO> getItinerariosDeUsuario(@RequestParam Long id_usuario) {

        return serviceItinerario.obtenerItinerariosPorUsuario(id_usuario);
    }

    @GetMapping("/itinerarios/favoritos")
    public Set<ItinerarioDTO> getItinerariosFavoritos(@RequestParam Long id_usuario){
        return serviceItinerario.getItinerariosFavoritos(id_usuario);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> getItineraryById() {
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getAllItineraries() {
//        return ResponseEntity.ok().build();
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateItinerary() {
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/generate")
//    public ResponseEntity<?> generateItinerary() {
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteItinerary() {
//        return ResponseEntity.ok().build();
//    }


}