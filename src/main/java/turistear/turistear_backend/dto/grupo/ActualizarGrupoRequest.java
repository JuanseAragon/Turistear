package turistear.turistear_backend.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body para actualizar un grupo existente.
 */
public record ActualizarGrupoRequest(
        @Schema(example = "Amigos del norte")
        @NotBlank(message = "El nombre del grupo es obligatorio")
        @Size(max = 35, message = "El nombre no puede superar los 35 caracteres")
        String nombre,

        @Schema(example = "Viaje a Jujuy 2026")
        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @Schema(example = "https://ejemplo.com/portada.jpg")
        @Size(max = 512, message = "La foto de portada no puede superar los 512 caracteres")
        String fotoPortada
) {
}
