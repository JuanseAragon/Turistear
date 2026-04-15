package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.UpdatePreferenciasRequest;
import turistear.turistear_backend.dto.UsuarioResponse;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AjustesService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse updatePreferencias(Long idUsuario, UpdatePreferenciasRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setTema(request.getTema());

        usuario = usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .tema(usuario.getTema())
                .build();
    }
}
