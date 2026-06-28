package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.model.Grupo;

/**
 * Vista resumida de un grupo para listados.
 */
public record GrupoResumenDTO(
        Long idGrupo,
        String nombre,
        String fotoPortada,
        Integer cantidadMiembros,
        String nombreCreador,
        boolean soyCreador,
        boolean tieneEncuestaAbierta
) {
    public static GrupoResumenDTO from(Grupo grupo, int cantidadMiembros,
                                       boolean soyCreador, boolean tieneEncuestaAbierta) {
        if (grupo == null) return null;
        return new GrupoResumenDTO(
                grupo.getIdGrupo(),
                grupo.getNombre(),
                grupo.getFotoPortada(),
                cantidadMiembros,
                grupo.getCreador().getNombre(),
                soyCreador,
                tieneEncuestaAbierta
        );
    }
}
