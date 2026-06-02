package turistear.turistear_backend.dto.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body para {@code PUT /favoritos/\{id\}}. Por ahora lo único que el
 * usuario puede editar a nivel cabezal son las fechas — el resto de
 * los datos (titulo, descripcion, foto, etc.) vive en el itinerario
 * del sistema referenciado.
 */
public record UpdateItinerarioUsuarioRequest(
        @Schema(example = "2026-07-15")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @Schema(example = "2026-07-22")
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin
) {
    @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio")
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) return true;
        return !fechaFin.isBefore(fechaInicio);
    }
}
