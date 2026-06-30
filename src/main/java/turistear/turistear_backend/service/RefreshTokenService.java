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
     * nuevo. Lanza {@link UnauthorizedException} si no existe, está revocado o venció.
     */
    @Transactional
    public RefreshToken validarYRotar(String token) {
        RefreshToken actual = refreshTokenRepository.findByTokenWithUsuario(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        if (!actual.isValido()) {
            throw new UnauthorizedException("Refresh token expirado o revocado");
        }

        actual.setRevoked(true);
        refreshTokenRepository.save(actual);

        return crear(actual.getUsuario());
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
