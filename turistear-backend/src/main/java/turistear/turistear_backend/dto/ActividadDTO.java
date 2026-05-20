package turistear.turistear_backend.dto;

import turistear.turistear_backend.model.Actividad;

public record ActividadDTO(
        Long idActividad,
        String nombre,
        String descripcion,
        String localidad,
        String direccion
) {
    public static ActividadDTO from(Actividad actividad) {
        if (actividad == null) return null;
        return new ActividadDTO(
                actividad.getIdActividad(),
                actividad.getNombre(),
                actividad.getDescripcion(),
                actividad.getLocalidad(),
                actividad.getDireccion()
        );
    }
}
