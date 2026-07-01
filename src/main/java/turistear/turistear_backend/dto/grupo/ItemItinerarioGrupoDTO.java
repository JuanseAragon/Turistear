package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.EstadoItemItinerarioGrupo;
import turistear.turistear_backend.model.AsistenciaItemGrupo;
import turistear.turistear_backend.model.ItemItinerarioGrupo;
import turistear.turistear_backend.model.Usuario;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actividad del itinerario de grupo con su estado de edición colaborativa y,
 * si está CONFIRMADO, la asistencia de todos los miembros del grupo.
 *
 * <p>La lista {@code asistencias} incluye a <strong>todos</strong> los miembros
 * (no solo a los que ya respondieron): el que no votó aparece con
 * {@code asiste = null}. Las listas "van" / "no van" / "sin responder" se
 * arman en el frontend filtrando este mismo array. Mientras el item está
 * PROPUESTO no tiene sentido mostrar asistencia, así que queda vacía.
 */
public record ItemItinerarioGrupoDTO(
        Long id,
        String nombreActividad,
        String descripcion,
        String localidad,
        String direccion,
        Integer dia,
        LocalTime hora,
        EstadoItemItinerarioGrupo estado,
        Long propuestoPorId,
        String nombrePropuestoPor,
        List<AsistenciaDTO> asistencias,
        Boolean miAsistencia,
        int cantidadConfirmados
) {
    /**
     * @param item                item a mapear
     * @param idUsuario           usuario que consulta (para calcular {@code miAsistencia})
     * @param miembros            roster completo del grupo
     * @param asistenciasDelItem  asistencias ya registradas para este item
     */
    public static ItemItinerarioGrupoDTO from(
            ItemItinerarioGrupo item,
            Long idUsuario,
            List<Usuario> miembros,
            List<AsistenciaItemGrupo> asistenciasDelItem) {
        if (item == null) return null;

        List<AsistenciaDTO> asistencias = List.of();
        Boolean miAsistencia = null;
        int cantidadConfirmados = 0;

        if (item.getEstado() == EstadoItemItinerarioGrupo.CONFIRMADO) {
            Map<Long, Boolean> porUsuario = asistenciasDelItem.stream()
                    .collect(Collectors.toMap(
                            a -> a.getUsuario().getIdUsuario(),
                            AsistenciaItemGrupo::isAsiste));
            asistencias = miembros.stream()
                    .map(u -> AsistenciaDTO.from(u, porUsuario.get(u.getIdUsuario())))
                    .toList();
            miAsistencia = porUsuario.get(idUsuario);
            cantidadConfirmados = (int) porUsuario.values().stream()
                    .filter(Boolean.TRUE::equals)
                    .count();
        }

        return new ItemItinerarioGrupoDTO(
                item.getId(),
                item.getNombreActividad(),
                item.getDescripcion(),
                item.getLocalidad(),
                item.getDireccion(),
                item.getDia(),
                item.getHora(),
                item.getEstado(),
                item.getPropuestoPor().getIdUsuario(),
                item.getPropuestoPor().getNombre(),
                asistencias,
                miAsistencia,
                cantidadConfirmados
        );
    }
}
