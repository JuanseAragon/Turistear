package turistear.turistear_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import turistear.turistear_backend.enumerable.Provincia;

@Data
public class LugarRequest {

    @NotNull(message = "La provincia es obligatoria")
    private Provincia provincia;

    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    private String direccion; // nullable, URL de Google Maps
}