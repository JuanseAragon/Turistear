package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.model.Grupo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista completa de un grupo con sus miembros.
 */
public record GrupoDTO(
        Long idGrupo,
        String nombre,
        String descripcion,
        String fotoPortada,
        Long creadorId,
        String nombreCreador,
        Integer cantidadMiembros,
        LocalDateTime fechaCreacion,
        boolean activo,
        boolean soyCreador,
        boolean tieneEncuestaAbierta,
        List<MiembroDTO> miembros
) {
    public static GrupoDTO from(Grupo grupo, int cantidadMiembros, boolean soyCreador,
                                boolean tieneEncuestaAbierta, List<MiembroDTO> miembros) {
        if (grupo == null) return null;
        return new GrupoDTO(
                grupo.getIdGrupo(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getFotoPortada(),
                grupo.getCreador().getIdUsuario(),
                grupo.getCreador().getNombre(),
                cantidadMiembros,
                grupo.getFechaCreacion(),
                grupo.isActivo(),
                soyCreador,
                tieneEncuestaAbierta,
                miembros
        );
    }
}
