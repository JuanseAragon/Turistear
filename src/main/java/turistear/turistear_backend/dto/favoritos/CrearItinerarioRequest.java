package turistear.turistear_backend.dto.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Body para {@code POST /itinerarios} — crear un itinerario propio desde
 * cero. Los items se agregan después con {@code POST /itinerarios/\{id\}/items}.
 * La duración en días se calcula del rango de fechas en el service.
 */
public record CrearItinerarioRequest(
        @Schema(example = "Mi finde en Mendoza")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo,

        @Schema(example = "Escapada de relax y vino")
        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @Schema(example = "MENDOZA")
        @NotNull(message = "La provincia es obligatoria")
        Provincia provincia,

        @Schema(example = "2026-07-15")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @Schema(example = "2026-07-18")
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @Schema(example = "https://mi-foto-de-portada.jpg")
        String fotoPortada,

        @Schema(example = "[\"NATURALEZA\", \"AVENTURA\"]")
        Set<CategoriaItinerario> etiquetas,

        @Schema(example = "[\"https://example.com/foto-1.jpg\", \"https://example.com/foto-2.jpg\"]")
        List<String> fotos
) {
    @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio")
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) return true;
        return !fechaFin.isBefore(fechaInicio);
    }
}
