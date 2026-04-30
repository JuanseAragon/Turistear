package turistear.turistear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import turistear.turistear_backend.enumerable.Provincia;

@Data
public class LugarRequest {

    @Schema(example = "MENDOZA")
    @NotNull(message = "La provincia es obligatoria")
    private Provincia provincia;

    @Schema(example = "Mendoza Capital")
    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    @Schema(example = "https://maps.google.com/?q=Mendoza+Capital")
    private String direccion; // nullable, URL de Google Maps
}
