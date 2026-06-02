package turistear.turistear_backend.dto.sistema;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioSistema;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista completa de un itinerario del sistema, con todos sus items
 * cargados. Usada en el endpoint de detalle
 * {@code GET /itinerario/\{id\}}. Para los listados (Explorar, Buscar,
 * Ranking) usar {@link ItinerarioSistemaResumenDTO} que omite los items.
 */
public record ItinerarioSistemaDTO(
        Long idItinerario,
        String titulo,
        String descripcion,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas,
        List<ItemItinerarioSistemaDTO> items,
        Integer likes
) {
    public static ItinerarioSistemaDTO from(ItinerarioSistema i) {
        if (i == null) return null;
        return new ItinerarioSistemaDTO(
                i.getIdItinerario(),
                i.getTitulo(),
                i.getDescripcion(),
                i.getProvincia(),
                i.getFechaInicio(),
                i.getFechaFin(),
                i.getFotoPortada(),
                i.getDuracionDias(),
                i.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .collect(Collectors.toSet()),
                i.getItems().stream()
                        .map(ItemItinerarioSistemaDTO::from)
                        .toList(),
                i.getLikes()
        );
    }
}
