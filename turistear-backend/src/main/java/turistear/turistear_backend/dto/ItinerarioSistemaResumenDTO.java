package turistear.turistear_backend.dto;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioSistema;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista resumida (sin items) de un itinerario del sistema. Es la
 * representación que viaja en las listas — Explorar, Buscar por
 * preferencias y Ranking — para no inflar el payload con todos los
 * items de cada itinerario.
 *
 * Para el detalle completo (incluyendo items) usar
 * {@link ItinerarioSistemaDTO}.
 */
public record ItinerarioSistemaResumenDTO(
        Long idItinerario,
        String titulo,
        String descripcion,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas
) {
    public static ItinerarioSistemaResumenDTO from(ItinerarioSistema i) {
        if (i == null) return null;
        return new ItinerarioSistemaResumenDTO(
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
                        .collect(Collectors.toSet())
        );
    }
}
