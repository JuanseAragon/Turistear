package turistear.turistear_backend.dto;

import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Itinerario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record ItinerarioDTO(
        Long id,
        String titulo,
        Provincia destino,
        String descripcion,
        boolean esPublico,
        LocalDateTime fechaCreacion,
        LocalDate fechaInicioViaje,
        LocalDate fechaFinViaje,
        List<ItemItinerarioDTO> items
) {
    public static ItinerarioDTO from(Itinerario itinerario) {
        return new ItinerarioDTO(
                itinerario.getIdItinerario(),
                itinerario.getTitulo(),
                itinerario.getDestino(),
                itinerario.getDescripcion(),
                itinerario.getEsPublico(),
                itinerario.getFechaCreacion(),
                itinerario.getFechaInicio(),
                itinerario.getFechaFin(),
                itinerario.getItemItinerarios()
                        .stream()
                        .map(ItemItinerarioDTO::from)
                        .toList()
        );
    }
}