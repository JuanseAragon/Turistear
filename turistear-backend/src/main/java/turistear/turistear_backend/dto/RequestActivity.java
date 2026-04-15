package turistear.turistear_backend.dto;

import java.util.Date;

public record RequestActivity(
        String nombre,
        String descripcion,
        String ubicacion,
        Date fecha_hora_final,
        Date fecha_hora_inicial
) {
}
