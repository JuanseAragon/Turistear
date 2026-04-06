package turistear.turistear_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class ControllerAjustes {

    @GetMapping("/downloads")
    public ResponseEntity<?> getDownloads() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/preferences")
    public ResponseEntity<?> updatePreferences() {
        return ResponseEntity.ok().build();
    }
}
