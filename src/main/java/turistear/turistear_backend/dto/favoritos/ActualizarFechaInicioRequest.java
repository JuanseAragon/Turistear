package turistear.turistear_backend.dto.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body para {@code PATCH /itinerarios/\{id\}/fecha-inicio}.
 *
 * <p>El usuario solo elige <strong>cuándo arranca</strong> el viaje. La
 * fecha de fin no se manda: se deriva de la duración en días del
 * itinerario ({@code fechaFin = fechaInicio + duracionDias - 1}), de modo
 * que días y fechas nunca quedan inconsistentes.
 */
public record ActualizarFechaInicioRequest(
        @Schema(example = "2026-07-15")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio
) {
}
