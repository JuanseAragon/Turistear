package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.ItinerarioGrupo;

import java.time.LocalDate;
import java.util.List;

/**
 * Itinerario compartido de un grupo con sus actividades.
 * {@code creadorOriginal*} es quién era el creador del grupo al generarse el
 * itinerario (trazabilidad) — NO necesariamente el líder actual del grupo.
 * Para saber quién puede confirmar/editar, el frontend usa {@code Group.soyCreador}.
 */
public record ItinerarioGrupoDTO(
        Long idItinerarioGrupo,
        String titulo,
        String descripcion,
        Provincia provincia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String fotoPortada,
        Integer duracionDias,
        Long creadorOriginalId,
        String nombreCreadorOriginal,
        Long idEncuestaOrigen,
        List<ItemItinerarioGrupoDTO> items
) {
    public static ItinerarioGrupoDTO from(ItinerarioGrupo it, List<ItemItinerarioGrupoDTO> items) {
        if (it == null) return null;
        return new ItinerarioGrupoDTO(
                it.getIdItinerarioGrupo(),
                it.getTitulo(),
                it.getDescripcion(),
                it.getProvincia(),
                it.getFechaInicio(),
                it.getFechaFin(),
                it.getFotoPortada(),
                it.getDuracionDias(),
                it.getCreador().getIdUsuario(),
                it.getCreador().getNombre(),
                it.getEncuestaOrigen() != null ? it.getEncuestaOrigen().getIdEncuesta() : null,
                items
        );
    }
}
