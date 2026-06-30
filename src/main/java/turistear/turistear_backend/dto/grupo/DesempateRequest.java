package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Body para resolver un empate eligiendo manualmente la opción ganadora.
 */
public record DesempateRequest(
        @Schema(description = "Id de la opción que se elige como ganadora", example = "3")
        @NotNull(message = "Debe elegir una opción ganadora")
        Long idOpcionGanadora
) {
}
