package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itinerario")
public class ControllerItinerary {

    @GetMapping("/{id}")
    public ResponseEntity<?> getItineraryById() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllItineraries() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItinerary() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateItinerary() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItinerary() {
        return ResponseEntity.ok().build();
    }


}