package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.auth.ChangePasswordRequest;
import turistear.turistear_backend.dto.usuario.EstadisticasUsuarioResponse;
import turistear.turistear_backend.dto.usuario.UpdatePerfilRequest;
import turistear.turistear_backend.dto.usuario.UsuarioResponse;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioUsuarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ItinerarioUsuarioRepository itinerarioUsuarioRepository;

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

    public EstadisticasUsuarioResponse getEstadisticas(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        List<Provincia> provincias = itinerarioUsuarioRepository.findProvinciasVisitadas(idUsuario);
        List<String> provinciasStr = provincias.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        int total = provincias.size();
        int porcentaje = total == 0 ? 0 : Math.round((total * 100f) / 24);

        Long dias = itinerarioUsuarioRepository.findDiasTotalesViajados(idUsuario);

        List<Provincia> ranking = itinerarioUsuarioRepository.findProvinciasRanking(idUsuario);
        String provinciaFavorita = ranking.isEmpty() ? null : ranking.get(0).name();

        Optional<String> etiqueta = itinerarioUsuarioRepository.findEtiquetaPredominante(idUsuario);

        return EstadisticasUsuarioResponse.builder()
                .provinciasVisitadas(provinciasStr)
                .totalProvincias(total)
                .porcentajeArgentina(porcentaje)
                .diasTotalesViajados(dias != null ? dias.intValue() : 0)
                .provinciaFavorita(provinciaFavorita)
                .etiquetaPredominante(etiqueta.orElse(null))
                .build();
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