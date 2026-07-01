package turistear.turistear_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import turistear.turistear_backend.exception.UnauthorizedException;
import turistear.turistear_backend.model.RefreshToken;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long REFRESH_EXPIRATION_MS = 604_800_000L; // 7 días
    private static final long VENTANA_GRACIA_MS = 60_000L;          // 60 s

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService service;

    @BeforeEach
    void configurarValores() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", REFRESH_EXPIRATION_MS);
        ReflectionTestUtils.setField(service, "ventanaGraciaRotacionMs", VENTANA_GRACIA_MS);
    }

    @Test
    void validarYRotar_tokenVigente_rotaYRevocaElViejo() {
        Usuario usuario = usuario(1L, "Ana");
        RefreshToken viejo = tokenVigente("token-viejo", usuario);
        when(refreshTokenRepository.findByTokenWithUsuario("token-viejo")).thenReturn(Optional.of(viejo));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken nuevo = service.validarYRotar("token-viejo");

        assertNotEquals("token-viejo", nuevo.getToken());
        assertSame(usuario, nuevo.getUsuario());
        assertTrue(viejo.isRevoked());
        assertEquals(nuevo.getToken(), viejo.getTokenReemplazo());
    }

    @Test
    void validarYRotar_tokenYaRotadoDentroDeGracia_devuelveElMismoHijo() {
        Usuario usuario = usuario(1L, "Ana");
        RefreshToken hijo = tokenVigente("token-hijo", usuario);
        RefreshToken viejo = tokenRotado("token-viejo", usuario, "token-hijo");
        when(refreshTokenRepository.findByTokenWithUsuario("token-viejo")).thenReturn(Optional.of(viejo));
        when(refreshTokenRepository.findByTokenWithUsuario("token-hijo")).thenReturn(Optional.of(hijo));

        RefreshToken resultado = service.validarYRotar("token-viejo");

        assertSame(hijo, resultado);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void validarYRotar_tokenRotadoFueraDeGracia_lanza401() {
        Usuario usuario = usuario(1L, "Ana");
        RefreshToken hijo = tokenVigente("token-hijo", usuario);
        hijo.setCreatedAt(Instant.now().minusMillis(VENTANA_GRACIA_MS * 2)); // rotado hace rato
        RefreshToken viejo = tokenRotado("token-viejo", usuario, "token-hijo");
        when(refreshTokenRepository.findByTokenWithUsuario("token-viejo")).thenReturn(Optional.of(viejo));
        when(refreshTokenRepository.findByTokenWithUsuario("token-hijo")).thenReturn(Optional.of(hijo));

        assertThrows(UnauthorizedException.class, () -> service.validarYRotar("token-viejo"));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void validarYRotar_tokenRevocadoPorLogout_sinReemplazo_lanza401() {
        Usuario usuario = usuario(1L, "Ana");
        RefreshToken viejo = tokenVigente("token-viejo", usuario);
        viejo.setRevoked(true); // logout: revocado sin rotación
        when(refreshTokenRepository.findByTokenWithUsuario("token-viejo")).thenReturn(Optional.of(viejo));

        assertThrows(UnauthorizedException.class, () -> service.validarYRotar("token-viejo"));
    }

    @Test
    void validarYRotar_tokenVencido_lanza401() {
        Usuario usuario = usuario(1L, "Ana");
        RefreshToken vencido = tokenVigente("token-vencido", usuario);
        vencido.setExpiresAt(Instant.now().minusSeconds(10));
        when(refreshTokenRepository.findByTokenWithUsuario("token-vencido")).thenReturn(Optional.of(vencido));

        assertThrows(UnauthorizedException.class, () -> service.validarYRotar("token-vencido"));
    }

    @Test
    void validarYRotar_tokenInexistente_lanza401() {
        when(refreshTokenRepository.findByTokenWithUsuario("desconocido")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.validarYRotar("desconocido"));
    }

    private Usuario usuario(Long id, String nombre) {
        return Usuario.builder().idUsuario(id).nombre(nombre).email(nombre + "@mail.com").build();
    }

    private RefreshToken tokenVigente(String token, Usuario usuario) {
        Instant ahora = Instant.now();
        return RefreshToken.builder()
                .token(token)
                .usuario(usuario)
                .createdAt(ahora)
                .expiresAt(ahora.plusMillis(REFRESH_EXPIRATION_MS))
                .revoked(false)
                .build();
    }

    private RefreshToken tokenRotado(String token, Usuario usuario, String tokenReemplazo) {
        RefreshToken rotado = tokenVigente(token, usuario);
        rotado.setRevoked(true);
        rotado.setTokenReemplazo(tokenReemplazo);
        return rotado;
    }
}
