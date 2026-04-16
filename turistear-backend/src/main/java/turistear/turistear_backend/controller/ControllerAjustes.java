package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.UpdatePreferenciasRequest;
import turistear.turistear_backend.dto.UsuarioResponse;
import turistear.turistear_backend.service.AjustesService;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class ControllerAjustes {

    private final AjustesService ajustesService;

    @PatchMapping("/preferences/{idUsuario}")
    public ResponseEntity<UsuarioResponse> updatePreferences(
            @PathVariable Long idUsuario,
            @Valid @RequestBody UpdatePreferenciasRequest request) {
        return ResponseEntity.ok(ajustesService.updatePreferencias(idUsuario, request));
    }
}
