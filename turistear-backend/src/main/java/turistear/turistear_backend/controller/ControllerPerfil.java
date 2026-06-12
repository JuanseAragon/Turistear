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
import turistear.turistear_backend.dto.auth.ChangePasswordRequest;
import turistear.turistear_backend.dto.common.ErrorResponse;
import turistear.turistear_backend.dto.usuario.UpdatePerfilRequest;
import turistear.turistear_backend.dto.usuario.UsuarioResponse;
import turistear.turistear_backend.service.UsuarioService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Consulta y gestión del perfil de usuario")
public class ControllerPerfil {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el perfil de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos del perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> updateUserInfo(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePerfilRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Cambiar la contraseña del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        usuarioService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }
}