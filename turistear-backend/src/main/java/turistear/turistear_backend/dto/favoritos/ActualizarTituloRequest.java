package turistear.turistear_backend.dto.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body para {@code PATCH /itinerarios/\{id\}/titulo}. Permite renombrar
 * un itinerario propio sin tocar el resto del cabezal (fechas, duración).
 */
public record ActualizarTituloRequest(
        @Schema(example = "Finde en Bariloche")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo
) {
}
