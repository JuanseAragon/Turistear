package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Body para marcar asistencia a una actividad ("voy" / "no voy").
 */
public record AsistenciaRequest(
        @Schema(example = "true")
        @NotNull(message = "Debés indicar si asistís o no")
        Boolean asiste
) {
}
