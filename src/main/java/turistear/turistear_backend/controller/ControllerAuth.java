package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.auth.AuthResponse;
import turistear.turistear_backend.dto.auth.ChangePasswordRequest;
import turistear.turistear_backend.dto.auth.LoginRequest;
import turistear.turistear_backend.dto.auth.RegisterRequest;
import turistear.turistear_backend.dto.auth.RefreshTokenRequest;
import turistear.turistear_backend.dto.common.ErrorResponse;
import turistear.turistear_backend.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login y gestión de credenciales")
public class ControllerAuth {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements // sin requerimiento global: este endpoint es público
    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @SecurityRequirements // sin requerimiento global: este endpoint es público
    @Operation(summary = "Iniciar sesión y obtener un JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @SecurityRequirements // público: la prueba de identidad es poseer un refresh token válido
    @Operation(summary = "Renovar el access token usando un refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado o revocado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refrescar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @SecurityRequirements // público: revocar un refresh token propio no requiere access token vigente
    @Operation(summary = "Cerrar sesión (revoca el refresh token)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password/{idUsuario}")
    @Operation(summary = "Cambiar la contraseña del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Contraseña actual incorrecta o no autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(
            @PathVariable Long idUsuario,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(idUsuario, request);
        return ResponseEntity.ok().build();
    }
}
