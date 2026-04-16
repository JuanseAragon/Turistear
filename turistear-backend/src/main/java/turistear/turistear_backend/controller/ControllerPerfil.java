package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.UpdatePerfilRequest;
import turistear.turistear_backend.dto.UsuarioResponse;
import turistear.turistear_backend.service.UsuarioService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ControllerPerfil {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> updateUserInfo(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePerfilRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
