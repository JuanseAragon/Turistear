package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class ControllerFavoritos {

    @GetMapping
    public ResponseEntity<?> getFavorites() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> addFavorite() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFavorite() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFavorite() {
        return ResponseEntity.ok().build();
    }
}
