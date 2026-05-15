package turistear.turistear_backend.dto;

import turistear.turistear_backend.model.ItemItinerarioUsuario;

import java.time.LocalTime;

/**
 * Item dentro de la copia personal del usuario. A diferencia del
 * {@link ItemItinerarioSistemaDTO}, no hay un objeto Actividad embebido:
 * los datos viven directamente acá porque el usuario puede inventar
 * actividades nuevas que no existen en el catálogo del sistema.
 */
public record ItemItinerarioUsuarioDTO(
        Long id,
        String nombreActividad,
        String descripcion,
        String localidad,
        String direccion,
        Integer dia,
        LocalTime hora
) {
    public static ItemItinerarioUsuarioDTO from(ItemItinerarioUsuario item) {
        if (item == null) return null;
        return new ItemItinerarioUsuarioDTO(
                item.getId(),
                item.getNombreActividad(),
                item.getDescripcion(),
                item.getLocalidad(),
                item.getDireccion(),
                item.getDia(),
                item.getHora()
        );
    }
}
