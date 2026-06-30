package turistear.turistear_backend.dto.favoritos;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista completa (con items) de un itinerario propio del usuario.
 * Usada en {@code GET /itinerarios/\{id\}} y {@code GET /itinerarios/activo}.
 *
 * Todos los datos son propios de la copia — ya no se leen del sistema.
 */
public record ItinerarioUsuarioDTO(
        Long idItinerarioUsuario,
        String titulo,
        String descripcion,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas,
        List<ItemItinerarioUsuarioDTO> items,
        boolean esPinned,
        boolean completado,
        List<FotoItinerarioUsuarioDTO> fotos
) {
    public static ItinerarioUsuarioDTO from(ItinerarioUsuario iu) {
        if (iu == null) return null;
        return new ItinerarioUsuarioDTO(
                iu.getIdItinerarioUsuario(),
                iu.getTitulo(),
                iu.getDescripcion(),
                iu.getProvincia(),
                iu.getFechaInicio(),
                iu.getFechaFin(),
                iu.getFotoPortada(),
                iu.getDuracionDias(),
                iu.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .collect(Collectors.toSet()),
                iu.getItems().stream()
                        .map(ItemItinerarioUsuarioDTO::from)
                        .toList(),
                iu.isEsPinned(),
                iu.isCompletado(),
                iu.getFotos().stream()
                        .map(FotoItinerarioUsuarioDTO::from)
                        .toList()
        );
    }
}
