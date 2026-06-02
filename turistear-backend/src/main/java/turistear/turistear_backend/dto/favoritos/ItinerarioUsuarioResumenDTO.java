package turistear.turistear_backend.dto.favoritos;

import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Etiqueta;
import turistear.turistear_backend.model.ItinerarioSistema;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vista resumida (sin items) de una copia personal del usuario.
 * Usada en {@code GET /favoritos} para listar todos los favoritos
 * sin inflar el payload.
 *
 * Incluye los metadatos que vienen del itinerario del sistema
 * referenciado (titulo, provincia, fotoPortada, etiquetas) más las
 * fechas <em>propias</em> de la copia del usuario.
 */
public record ItinerarioUsuarioResumenDTO(
        Long idItinerarioUsuario,
        Long idItinerarioSistema,
        String titulo,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Set<CategoriaItinerario> etiquetas
) {
    public static ItinerarioUsuarioResumenDTO from(ItinerarioUsuario iu) {
        if (iu == null) return null;
        ItinerarioSistema sis = iu.getItinerarioSistema();
        return new ItinerarioUsuarioResumenDTO(
                iu.getIdItinerarioUsuario(),
                sis.getIdItinerario(),
                sis.getTitulo(),
                sis.getProvincia(),
                iu.getFechaInicio(),
                iu.getFechaFin(),
                sis.getFotoPortada(),
                sis.getDuracionDias(),
                sis.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .collect(Collectors.toSet())
        );
    }
}
