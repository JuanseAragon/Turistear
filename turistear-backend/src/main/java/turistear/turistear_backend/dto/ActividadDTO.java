package turistear.turistear_backend.dto;

import turistear.turistear_backend.model.Actividad;

public record ActividadDTO(
        String nombre,
        String descripcion,
        String ubicacion
) {
    // Método estático que convierte una Actividad en ActividadDTO
    public static ActividadDTO from(Actividad actividad) {
        if (actividad == null) return null;
        return new ActividadDTO(
                actividad.getNombre(),
                actividad.getDescripcion(),
                actividad.getUbicacion()
        );
    }
}
