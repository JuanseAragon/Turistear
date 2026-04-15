package turistear.turistear_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;

public record RequestGeneration(
                @NotBlank(message = "El destino es obligatorio")
        String destiny_location,
                @NotNull(message = "La fecha de inicio es obligatoria")
        Date start,
                @NotNull(message = "La fecha de fin es obligatoria")
        Date end,
                @Size(max = 15, message = "Se permiten hasta 15 tags")
                List<String> tags,
                @Positive(message = "El id de usuario debe ser positivo")
        Long idUsuario
) {

        @AssertTrue(message = "La fecha de inicio no puede ser posterior a la fecha de fin")
        public boolean isDateRangeValid() {
                if (start == null || end == null) {
                        return true;
                }
                return !start.after(end);
        }
}
