package turistear.turistear_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;

public record ItinerarioRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 120, message = "El titulo no puede superar los 120 caracteres")
        String titulo,

        @NotNull(message = "El destino (provincia) es obligatorio")
        Provincia destino,

        @Size(max = 2000, message = "La descripcion no puede superar los 2000 caracteres")
        String descripcion,

        Boolean esPublico,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

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
