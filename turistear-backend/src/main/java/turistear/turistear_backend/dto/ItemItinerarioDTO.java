package turistear.turistear_backend.dto;

import turistear.turistear_backend.model.ItemItinerario;

public record ItemItinerarioDTO(
        ActividadDTO actividad
) {
    public static ItemItinerarioDTO from(ItemItinerario item) {
        if (item == null) return null;
        return new ItemItinerarioDTO(
                ActividadDTO.from(item.getActividad())
        );
    }
}
