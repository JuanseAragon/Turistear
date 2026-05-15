package turistear.turistear_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import turistear.turistear_backend.exception.UnauthorizedException;
import turistear.turistear_backend.repository.UsuarioRepository;

/**
 * Helper para extraer el {@code idUsuario} del usuario autenticado a
 * partir del {@link Authentication} que Spring Security inyecta en cada
 * controller.
 *
 * <p>Flujo:
 * <ol>
 *   <li>El {@code JwtAuthenticationFilter} valida el token y popula el
 *       {@code SecurityContext} con un {@code Authentication} cuyo
 *       {@code name} es el email del usuario (subject del JWT).</li>
 *   <li>Acá hacemos un lookup por email contra {@link UsuarioRepository}
 *       para resolver el id numérico — única consulta extra a DB por
 *       request.</li>
 * </ol>
 *
 * Si más adelante se quiere eliminar ese lookup, la mejora es cambiar
 * el principal de Spring Security a una clase custom que ya tenga el
 * idUsuario embebido (ver {@link CustomUserDetailsService}).
 */
@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve el {@code idUsuario} del usuario autenticado. Tira 401
     * si no hay autenticación o si el email del token no corresponde a
     * ningún usuario activo en la base.
     */
    public Long getIdUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Se requiere autenticación");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Token válido pero el usuario ya no existe"))
                .getIdUsuario();
    }
}
