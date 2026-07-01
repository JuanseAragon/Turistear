package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.exception.UnauthorizedException;
import turistear.turistear_backend.model.RefreshToken;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Gestiona el ciclo de vida de los refresh tokens (stateful). El access token
 * lo maneja {@link turistear.turistear_backend.security.JwtService}; acá vive
 * todo lo relacionado al token opaco persistido: creación, validación, rotación
 * y revocación.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /**
     * Ventana durante la cual un refresh token recién rotado sigue aceptando el
     * token viejo. Absorbe refreshes concurrentes / reintentos: en vez de tirar
     * 401 devolvemos el token hijo que ya se emitió.
     */
    @Value("${jwt.refresh-grace-period}")
    private long ventanaGraciaRotacionMs;

    /** Crea y persiste un refresh token nuevo (UUID opaco) para el usuario. */
    @Transactional
    public RefreshToken crear(Usuario usuario) {
        Instant now = Instant.now();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .createdAt(now)
                .expiresAt(now.plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Valida el refresh token recibido y lo rota: revoca el viejo y devuelve uno
     * nuevo. Es idempotente ante refreshes concurrentes: si el token ya fue rotado
     * hace muy poco (dentro de la ventana de gracia), devuelve el mismo token hijo
     * en lugar de fallar. Lanza {@link UnauthorizedException} si no existe, venció,
     * o fue revocado por logout / fuera de la ventana de gracia.
     */
    @Transactional
    public RefreshToken validarYRotar(String tokenPresentado) {
        RefreshToken tokenActual = refreshTokenRepository.findByTokenWithUsuario(tokenPresentado)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        // Caso normal: el token está vigente -> lo rotamos.
        if (tokenActual.isValido()) {
            RefreshToken tokenNuevo = crear(tokenActual.getUsuario());
            tokenActual.setRevoked(true);
            tokenActual.setTokenReemplazo(tokenNuevo.getToken());
            refreshTokenRepository.save(tokenActual);
            return tokenNuevo;
        }

        // Caso concurrencia/reintento: ya fue rotado hace muy poco -> devolvemos el mismo hijo.
        RefreshToken reemplazo = reemplazoDentroDeGracia(tokenActual);
        if (reemplazo != null) {
            return reemplazo;
        }

        throw new UnauthorizedException("Refresh token expirado o revocado");
    }

    /**
     * Si {@code tokenViejo} fue rotado dentro de la ventana de gracia y su token
     * hijo sigue vigente, lo devuelve; en cualquier otro caso devuelve {@code null}
     * (revocado por logout, vencido, o rotado hace demasiado tiempo).
     */
    private RefreshToken reemplazoDentroDeGracia(RefreshToken tokenViejo) {
        if (tokenViejo.getTokenReemplazo() == null) {
            return null;
        }
        return refreshTokenRepository.findByTokenWithUsuario(tokenViejo.getTokenReemplazo())
                .filter(RefreshToken::isValido)
                .filter(hijo -> hijo.getCreatedAt().plusMillis(ventanaGraciaRotacionMs).isAfter(Instant.now()))
                .orElse(null);
    }

    /** Revoca un refresh token puntual (logout). Tolerante: si no existe, no hace nada. */
    @Transactional
    public void revocar(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    /** Revoca todos los tokens activos de un usuario (cambio de contraseña / logout global). */
    @Transactional
    public void revocarTodosDe(Usuario usuario) {
        refreshTokenRepository.revocarTodosDe(usuario);
    }
}
