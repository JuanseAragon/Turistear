package turistear.turistear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @Schema(example = "Password123")
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contraseniaActual;

    @Schema(example = "NewPassword456")
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número"
    )
    private String contraseniaNueva;
}
