package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.*;
import turistear.turistear_backend.enumerable.TipoTema;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.exception.UnauthorizedException;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.UsuarioRepository;
import turistear.turistear_backend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .contrasenia(passwordEncoder.encode(request.getContrasenia()))
                .fechaNacimiento(request.getFechaNacimiento())
                .tema(TipoTema.CLARO)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContrasenia())) {
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }

    public void changePassword(Long idUsuario, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new UnauthorizedException("La contraseña actual es incorrecta");
        }

        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);
    }
}
