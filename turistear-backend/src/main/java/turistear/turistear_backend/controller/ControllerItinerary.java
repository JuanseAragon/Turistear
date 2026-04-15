package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.dto.ResponseGeneration;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.service.itinerario.ItinerarioService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/itinerario")
public class ControllerItinerary {

    @Autowired
    private ItinerarioService itinerarioService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getItineraryById(@PathVariable Long id) {
        Optional<Itinerario> itinerario = this.itinerarioService.getItinerarioById(id);
        return itinerario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> getAllItineraries() {
        List<Itinerario> itinerarios = this.itinerarioService.getPublicItinerarios();
        return ResponseEntity.ok(itinerarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItinerary(@PathVariable Long id, @Valid @RequestBody RequestUpdateItinerary request) {
        Optional<Itinerario> updatedItinerario = this.itinerarioService.updateItinerary(id, request);
        return updatedItinerario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateItinerary(@Valid @RequestBody RequestGeneration prompt) {
        ResponseGeneration response = this.itinerarioService.generate(prompt);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItinerary(@PathVariable Long id) {
        boolean deleted = this.itinerarioService.deleteItinerarioById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

}