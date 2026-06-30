package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.auth.AuthResponse;
import turistear.turistear_backend.dto.auth.ChangePasswordRequest;
import turistear.turistear_backend.dto.auth.LoginRequest;
import turistear.turistear_backend.dto.auth.RefreshTokenRequest;
import turistear.turistear_backend.dto.auth.RegisterRequest;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.exception.UnauthorizedException;
import turistear.turistear_backend.model.RefreshToken;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.UsuarioRepository;
import turistear.turistear_backend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .contrasenia(passwordEncoder.encode(request.getContrasenia()))
                .build();

        usuario = usuarioRepository.save(usuario);

        return emitirTokens(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContrasenia())) {
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }

        return emitirTokens(usuario);
    }

    /**
     * Renueva el access token a partir de un refresh token válido. Rota el refresh
     * token (revoca el viejo, emite uno nuevo) y devuelve el par completo.
     */
    public AuthResponse refrescar(RefreshTokenRequest request) {
        RefreshToken nuevo = refreshTokenService.validarYRotar(request.getRefreshToken());
        Usuario usuario = nuevo.getUsuario();
        String accessToken = jwtService.generateAccessToken(usuario);
        return buildAuthResponse(usuario, accessToken, nuevo.getToken());
    }

    /** Cierra sesión revocando el refresh token recibido. Idempotente. */
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revocar(request.getRefreshToken());
    }

    public void changePassword(Long idUsuario, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new UnauthorizedException("La contraseña actual es incorrecta");
        }

        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);

        // Al cambiar la contraseña invalidamos todas las sesiones activas: las demás
        // sesiones (otros dispositivos) tendrán que volver a loguearse.
        refreshTokenService.revocarTodosDe(usuario);
    }

    /** Genera un access token nuevo y un refresh token nuevo para el usuario, y arma la respuesta. */
    private AuthResponse emitirTokens(Usuario usuario) {
        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = refreshTokenService.crear(usuario).getToken();
        return buildAuthResponse(usuario, accessToken, refreshToken);
    }

    private AuthResponse buildAuthResponse(Usuario usuario, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .fotoPerfil(usuario.getFotoPerfil())
                .build();
    }
}
