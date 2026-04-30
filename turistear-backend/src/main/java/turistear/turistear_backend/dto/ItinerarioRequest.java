package turistear.turistear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;

public record ItinerarioRequest(
        @Schema(example = "Aventura en Mendoza")
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 120, message = "El titulo no puede superar los 120 caracteres")
        String titulo,

        @Schema(example = "MENDOZA")
        @NotNull(message = "El destino (provincia) es obligatorio")
        Provincia destino,

        @Schema(example = "Recorrido de 7 días por bodegas y montañas de Mendoza")
        @Size(max = 2000, message = "La descripcion no puede superar los 2000 caracteres")
        String descripcion,

        @Schema(example = "true")
        Boolean esPublico,

        @Schema(example = "2026-06-01")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @Schema(example = "2026-06-07")
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @Schema(example = "1")
        @NotNull(message = "El id del creador es obligatorio")
        @Positive(message = "El id del creador debe ser positivo")
        Long idCreador  // ← Long porque tu Usuario usa Long
) {

        @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio")
        public boolean isRangoFechasValido() {
                if (fechaInicio == null || fechaFin == null) {
                        return true; // ya lo cubre @NotNull en cada campo
                }
                return !fechaFin.isBefore(fechaInicio);
        }
}
