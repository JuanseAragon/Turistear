package turistear.turistear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(example = "juan.perez@example.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9\\-]{2,}(\\.[a-zA-Z0-9\\-]+)*\\.[a-zA-Z]{2,}$",
        message = "El email no tiene un formato válido"
    )
    private String email;

    @Schema(example = "Password123")
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;
}
