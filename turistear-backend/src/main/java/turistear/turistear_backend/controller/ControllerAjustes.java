package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ErrorResponse;
import turistear.turistear_backend.dto.UpdatePreferenciasRequest;
import turistear.turistear_backend.dto.UsuarioResponse;
import turistear.turistear_backend.service.AjustesService;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
@Tag(name = "Ajustes", description = "Preferencias y configuración del usuario")
public class ControllerAjustes {

    private final AjustesService ajustesService;

    @PatchMapping("/preferences/{idUsuario}")
    @Operation(summary = "Actualizar las preferencias del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias actualizadas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> updatePreferences(
            @PathVariable Long idUsuario,
            @Valid @RequestBody UpdatePreferenciasRequest request) {
        return ResponseEntity.ok(ajustesService.updatePreferencias(idUsuario, request));
    }
}
