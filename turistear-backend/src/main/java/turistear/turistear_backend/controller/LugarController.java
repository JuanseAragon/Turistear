package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.LugarRequest;
import turistear.turistear_backend.dto.LugarResponse;
import turistear.turistear_backend.service.LugarService;

import java.util.List;

@RestController
@RequestMapping("/lugares")
@RequiredArgsConstructor
public class LugarController {

    private final LugarService lugarService;

    // Crea un nuevo lugar con provincia, localidad y dirección (URL Maps)
    @PostMapping
    public ResponseEntity<LugarResponse> create(@Valid @RequestBody LugarRequest request) {
        return ResponseEntity.ok(lugarService.create(request));
    }

    // Devuelve todos los lugares registrados (útil para mostrar opciones al crear una actividad)
    @GetMapping
    public ResponseEntity<List<LugarResponse>> getAll() {
        return ResponseEntity.ok(lugarService.getAll());
    }

    // Devuelve un lugar específico por su ID
    @GetMapping("/{id}")
    public ResponseEntity<LugarResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lugarService.getById(id));
    }

    // Actualiza los datos de un lugar existente
    @PutMapping("/{id}")
    public ResponseEntity<LugarResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LugarRequest request) {
        return ResponseEntity.ok(lugarService.update(id, request));
    }

    // Elimina un lugar por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lugarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}