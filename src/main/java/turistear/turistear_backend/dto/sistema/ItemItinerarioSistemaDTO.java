package turistear.turistear_backend.dto.sistema;

import turistear.turistear_backend.model.ItemItinerarioSistema;

import java.time.LocalTime;

/**
 * Item de un itinerario del sistema. Embebe la actividad asociada
 * (no solo su id) para que el frontend pueda renderizar la card sin
 * un round trip adicional.
 */
public record ItemItinerarioSistemaDTO(
        Long id,
        Integer dia,
        LocalTime hora,
        ActividadDTO actividad
) {
    public static ItemItinerarioSistemaDTO from(ItemItinerarioSistema item) {
        if (item == null) return null;
        return new ItemItinerarioSistemaDTO(
                item.getId(),
                item.getDia(),
                item.getHora(),
                ActividadDTO.from(item.getActividad())
        );
    }
}
