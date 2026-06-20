package turistear.turistear_backend.dto.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AgregarFotoItinerarioRequest(
        @Schema(example = "https://example.com/foto.jpg")
        @NotBlank(message = "La URL de la foto es obligatoria")
        String url
) {
}
