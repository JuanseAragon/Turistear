package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.model.CodigoInvitacion;

import java.time.LocalDateTime;

/**
 * Código de invitación activo con su expiración.
 */
public record CodigoInvitacionDTO(
        String codigo,
        LocalDateTime fechaExpiracion
) {
    public static CodigoInvitacionDTO from(CodigoInvitacion codigo) {
        if (codigo == null) return null;
        return new CodigoInvitacionDTO(
                codigo.getCodigo(),
                codigo.getFechaExpiracion()
        );
    }
}
