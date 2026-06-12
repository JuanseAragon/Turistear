package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.auth.ChangePasswordRequest;
import turistear.turistear_backend.dto.usuario.UpdatePerfilRequest;
import turistear.turistear_backend.dto.usuario.UsuarioResponse;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse getById(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    public UsuarioResponse update(Long idUsuario, UpdatePerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Si el email cambió, validamos que no esté en uso por otro usuario
        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail())) {
            boolean emailEnUso = usuarioRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getIdUsuario().equals(idUsuario))
                    .isPresent();
            if (emailEnUso) {
                throw new BadRequestException("El email ya está en uso");
            }
            usuario.setEmail(request.getEmail());
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setFotoPerfil(request.getFotoPerfil());

        return toResponse(usuarioRepository.save(usuario));
    }

    public void changePassword(Long idUsuario, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // La contraseña actual debe coincidir con la guardada (hasheada)
        if (!passwordEncoder.matches(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // La nueva no puede ser igual a la actual
        if (passwordEncoder.matches(request.getContraseniaNueva(), usuario.getContrasenia())) {
            throw new BadRequestException("La nueva contraseña debe ser distinta a la actual");
        }

        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .fotoPerfil(usuario.getFotoPerfil())
                .build();
    }
}