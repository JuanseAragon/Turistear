package turistear.turistear_backend.dto;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioSistema;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista completa de una copia personal del usuario, incluyendo todos
 * sus items. Usada en {@code GET /favoritos/\{id\}} y
 * {@code GET /favoritos/activo}.
 */
public record ItinerarioUsuarioDTO(
        Long idItinerarioUsuario,
        Long idItinerarioSistema,
        String titulo,
        String descripcion,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas,
        List<ItemItinerarioUsuarioDTO> items
) {
    public static ItinerarioUsuarioDTO from(ItinerarioUsuario iu) {
        if (iu == null) return null;
        ItinerarioSistema sis = iu.getItinerarioSistema();
        return new ItinerarioUsuarioDTO(
                iu.getIdItinerarioUsuario(),
                sis.getIdItinerario(),
                sis.getTitulo(),
                sis.getDescripcion(),
                sis.getProvincia(),
                iu.getFechaInicio(),
                iu.getFechaFin(),
                sis.getFotoPortada(),
                sis.getDuracionDias(),
                sis.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .collect(Collectors.toSet()),
                iu.getItems().stream()
                        .map(ItemItinerarioUsuarioDTO::from)
                        .toList()
        );
    }
}
