package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * Body para proponer/editar una actividad del itinerario de grupo.
 * Mismo shape que {@code ItemFavoritoRequest} del itinerario individual.
 */
public record ItemItinerarioGrupoRequest(
        @Schema(example = "Caminata en el Cerro Catedral")
        @NotBlank(message = "El nombre de la actividad es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
        String nombreActividad,

        @Schema(example = "Recorrido guiado de media jornada")
        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @Schema(example = "Bariloche")
        @Size(max = 120, message = "La localidad no puede superar los 120 caracteres")
        String localidad,

        @Schema(example = "Av. Bustillo km 21")
        @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
        String direccion,

        @Schema(example = "1")
        @NotNull(message = "El día es obligatorio")
        @Positive(message = "El día debe ser positivo (1, 2, 3, ...)")
        Integer dia,

        @Schema(example = "09:30")
        LocalTime hora
) {
}
