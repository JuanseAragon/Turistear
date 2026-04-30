package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.UpdatePerfilRequest;
import turistear.turistear_backend.dto.UsuarioResponse;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse getById(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        return toResponse(usuario);
    }

    public UsuarioResponse update(Long idUsuario, UpdatePerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        usuario.setNombre(request.getNombre());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setFotoPerfil(request.getFotoPerfil());

        return toResponse(usuarioRepository.save(usuario));
    }

    public void delete(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            throw new ConflictException("El usuario ya fue eliminado");
        }

        usuario.setEmail("deleted_" + idUsuario + "@deleted.local");
        usuario.setEliminado(true);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .tema(usuario.getTema())
                .fotoPerfil(usuario.getFotoPerfil())
                .build();
    }
}
