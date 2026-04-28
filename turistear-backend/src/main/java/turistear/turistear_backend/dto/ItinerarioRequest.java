package turistear.turistear_backend.dto;

import java.time.LocalDate;

public record ItinerarioRequest(
        String titulo,
        String destino,
        String descripcion,
        Boolean esPublico,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long idCreador  // ← Long porque tu Usuario usa Long
) {}
