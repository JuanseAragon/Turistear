package turistear.turistear_backend.dto.grupo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Solicitud para cambiar la fecha de inicio de un itinerario de grupo.
 * La fecha de fin se recalcula automáticamente según la duración en días.
 */
public record ActualizarFechaItinerarioGrupoRequest(
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio
) {
}
