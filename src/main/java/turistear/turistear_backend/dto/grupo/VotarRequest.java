package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Body para emitir un voto en una encuesta.
 */
public record VotarRequest(
        @Schema(description = "Id de la opción elegida", example = "2")
        @NotNull(message = "Debe elegir una opción")
        Long idOpcion
) {
}
