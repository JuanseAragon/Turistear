package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.model.Usuario;

/**
 * Asistencia de un miembro a una actividad del itinerario de grupo.
 * {@code asiste}: {@code true} = va, {@code false} = no va, {@code null} = todavía no respondió.
 */
public record AsistenciaDTO(
        Long usuarioId,
        String nombreUsuario,
        String fotoPerfil,
        Boolean asiste
) {
    public static AsistenciaDTO from(Usuario usuario, Boolean asiste) {
        if (usuario == null) return null;
        return new AsistenciaDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getFotoPerfil(),
                asiste
        );
    }
}
