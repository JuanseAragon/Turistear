package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Body para crear una encuesta con las opciones propuestas.
 */
public record CrearEncuestaRequest(
        @Schema(description = "Nombre opcional de la encuesta")
        @Size(max = 35, message = "El nombre no puede superar los 35 caracteres")
        String nombre,

        @Schema(description = "Opciones propuestas para la encuesta")
        @NotEmpty(message = "La encuesta debe tener al menos una opción")
        List<OpcionSolicitud> opciones
) {
    /**
     * Referencia a un itinerario del sistema o a un itinerario de usuario.
     * Solo uno de los dos ids debe estar presente.
     */
    public record OpcionSolicitud(
            @Schema(description = "Id del itinerario del sistema", example = "12")
            Long idItinerarioSistema,

            @Schema(description = "Id del itinerario propio del usuario", example = "34")
            Long idItinerarioUsuario
    ) {
    }
}
