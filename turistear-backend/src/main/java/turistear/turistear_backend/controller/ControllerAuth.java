package turistear.turistear_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.AuthResponse;
import turistear.turistear_backend.dto.ChangePasswordRequest;
import turistear.turistear_backend.dto.LoginRequest;
import turistear.turistear_backend.dto.RegisterRequest;
import turistear.turistear_backend.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ControllerAuth {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password/{idUsuario}")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long idUsuario,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(idUsuario, request);
        return ResponseEntity.ok().build();
    }
}
