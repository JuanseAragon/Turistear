package turistear.turistear_backend.dto.favoritos;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista resumida (sin items) de un itinerario propio del usuario.
 * Usada en {@code GET /itinerarios} para listar sin inflar el payload.
 *
 * Todos los datos son propios de la copia (titulo, provincia, foto,
 * etiquetas) — ya no se leen del itinerario del sistema.
 */
public record ItinerarioUsuarioResumenDTO(
        Long idItinerarioUsuario,
        String titulo,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas,
        boolean esPinned,
        boolean completado
) {
    public static ItinerarioUsuarioResumenDTO from(ItinerarioUsuario iu) {
        if (iu == null) return null;
        return new ItinerarioUsuarioResumenDTO(
                iu.getIdItinerarioUsuario(),
                iu.getTitulo(),
                iu.getProvincia(),
                iu.getFechaInicio(),
                iu.getFechaFin(),
                iu.getFotoPortada(),
                iu.getDuracionDias(),
                iu.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .collect(Collectors.toSet()),
                iu.isEsPinned(),
                iu.isCompletado()
        );
    }
}
