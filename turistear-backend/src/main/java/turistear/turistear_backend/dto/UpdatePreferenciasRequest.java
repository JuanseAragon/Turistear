package turistear.turistear_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePreferenciasRequest {

    @NotBlank(message = "El tema es obligatorio")
    private String tema;
}
