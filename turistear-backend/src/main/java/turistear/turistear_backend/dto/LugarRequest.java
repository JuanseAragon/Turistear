package turistear.turistear_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LugarRequest {

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    private String direccion; // nullable, URL de Google Maps
}