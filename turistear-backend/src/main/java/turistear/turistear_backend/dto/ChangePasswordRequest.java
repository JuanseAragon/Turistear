package turistear.turistear_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contraseniaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número"
    )
    private String contraseniaNueva;
}
