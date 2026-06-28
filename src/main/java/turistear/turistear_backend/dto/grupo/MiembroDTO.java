package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.model.MiembroGrupo;

/**
 * Datos de un miembro dentro de un grupo.
 */
public record MiembroDTO(
        Long idUsuario,
        String nombre,
        String fotoPerfil,
        RolGrupo rol
) {
    public static MiembroDTO from(MiembroGrupo miembro) {
        if (miembro == null) return null;
        return new MiembroDTO(
                miembro.getUsuario().getIdUsuario(),
                miembro.getUsuario().getNombre(),
                miembro.getUsuario().getFotoPerfil(),
                miembro.getRol()
        );
    }
}
