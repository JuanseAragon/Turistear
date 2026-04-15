package turistear.turistear_backend.dto;

import java.util.Date;
import java.util.List;

public record ResponseGeneration(
        String nombre,
        Date fecha_inicio,
        Date fecha_final,
        List<RequestActivity> actividades

) {}
