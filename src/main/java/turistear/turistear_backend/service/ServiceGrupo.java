package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.grupo.*;
import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de grupos: creación, invitaciones, membresía y borrado.
 */
@Service
@RequiredArgsConstructor
public class ServiceGrupo {

    private static final int LONGITUD_CODIGO = 8;
    private static final int MINUTOS_VALIDEZ_CODIGO = 15;
    private static final String CARACTERES_CODIGO = "0123456789ABCDEF";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GrupoRepository grupoRepo;
    private final MiembroGrupoRepository miembroRepo;
    private final CodigoInvitacionRepository codigoRepo;
    private final EncuestaRepository encuestaRepo;
    private final OpcionEncuestaRepository opcionRepo;
    private final VotoRepository votoRepo;
    private final UsuarioRepository usuarioRepo;

    /* ---------------------------------------------------------------- *
     *  POST /grupos
     * ---------------------------------------------------------------- */

    @Transactional
    public GrupoDTO crearGrupo(Long idUsuario, CrearGrupoRequest request) {
        Usuario usuario = buscarUsuario(idUsuario);

        Grupo grupo = Grupo.builder()
                .creador(usuario)
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .fotoPortada(request.fotoPortada())
                .fechaCreacion(LocalDateTime.now())
                .activo(true)
                .build();

        Grupo guardado = grupoRepo.save(grupo);

        MiembroGrupo creador = MiembroGrupo.builder()
                .grupo(guardado)
                .usuario(usuario)
                .rol(RolGrupo.CREADOR)
                .fechaUnion(LocalDateTime.now())
                .build();
        miembroRepo.save(creador);

        return GrupoDTO.from(guardado, 1, true, false,
                List.of(MiembroDTO.from(creador)));
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/unirse/{codigo}
     * ---------------------------------------------------------------- */

    @Transactional
    public GrupoDTO unirseAGrupo(Long idUsuario, String codigo) {
        CodigoInvitacion invitacion = codigoRepo.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Código de invitación no válido"));

        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código de invitación expiró");
        }

        Grupo grupo = invitacion.getGrupo();
        Usuario usuario = buscarUsuario(idUsuario);

        if (miembroRepo.existsByGrupo_IdGrupoAndUsuario_IdUsuario(grupo.getIdGrupo(), idUsuario)) {
            throw new ConflictException("Ya sos miembro de este grupo");
        }

        MiembroGrupo nuevo = MiembroGrupo.builder()
                .grupo(grupo)
                .usuario(usuario)
                .rol(RolGrupo.MIEMBRO)
                .fechaUnion(LocalDateTime.now())
                .build();
        miembroRepo.save(nuevo);

        long cantidad = miembroRepo.countByGrupo_IdGrupo(grupo.getIdGrupo());
        boolean soyCreador = grupo.getCreador().getIdUsuario().equals(idUsuario);
        boolean tieneEncuestaAbierta = encuestaRepo.existsByGrupo_IdGrupoAndEstado(
                grupo.getIdGrupo(), EstadoEncuesta.ABIERTA);
        return GrupoDTO.from(grupo, (int) cantidad, soyCreador, tieneEncuestaAbierta,
                miembroRepo.findByGrupo_IdGrupo(grupo.getIdGrupo()).stream()
                        .map(MiembroDTO::from)
                        .toList());
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public List<GrupoResumenDTO> listarMisGrupos(Long idUsuario) {
        return miembroRepo.findByUsuario_IdUsuario(idUsuario).stream()
                .map(m -> {
                    Grupo grupo = m.getGrupo();
                    boolean soyCreador = grupo.getCreador().getIdUsuario().equals(idUsuario);
                    boolean tieneEncuestaAbierta = encuestaRepo.existsByGrupo_IdGrupoAndEstado(
                            grupo.getIdGrupo(), EstadoEncuesta.ABIERTA);
                    return GrupoResumenDTO.from(grupo,
                            (int) miembroRepo.countByGrupo_IdGrupo(grupo.getIdGrupo()),
                            soyCreador, tieneEncuestaAbierta);
                })
                .toList();
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{id}
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public GrupoDTO obtenerDetalle(Long idUsuario, Long idGrupo) {
        verificarMiembro(idGrupo, idUsuario);
        Grupo grupo = buscarGrupo(idGrupo);
        List<MiembroDTO> miembros = miembroRepo.findByGrupo_IdGrupo(idGrupo).stream()
                .map(MiembroDTO::from)
                .toList();
        boolean soyCreador = grupo.getCreador().getIdUsuario().equals(idUsuario);
        boolean tieneEncuestaAbierta = encuestaRepo.existsByGrupo_IdGrupoAndEstado(
                idGrupo, EstadoEncuesta.ABIERTA);
        return GrupoDTO.from(grupo, miembros.size(), soyCreador, tieneEncuestaAbierta, miembros);
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /grupos/{id}/salir
     * ---------------------------------------------------------------- */

    @Transactional
    public void salirDeGrupo(Long idUsuario, Long idGrupo) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        Grupo grupo = miembro.getGrupo();

        List<MiembroGrupo> restantes = miembroRepo.findByGrupo_IdGrupoOrderByFechaUnionAsc(idGrupo).stream()
                .filter(m -> !m.getUsuario().getIdUsuario().equals(idUsuario))
                .toList();

        if (restantes.isEmpty()) {
            eliminarGrupoYRelacionados(idGrupo);
            return;
        }

        if (miembro.getRol() == RolGrupo.CREADOR) {
            MiembroGrupo nuevoCreador = restantes.getFirst();
            nuevoCreador.setRol(RolGrupo.CREADOR);
            miembroRepo.save(nuevoCreador);
        }

        miembroRepo.delete(miembro);
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{id}/codigo
     * ---------------------------------------------------------------- */

    @Transactional
    public CodigoInvitacionDTO generarCodigoInvitacion(Long idUsuario, Long idGrupo) {
        verificarCreador(idGrupo, idUsuario);
        Grupo grupo = buscarGrupo(idGrupo);

        String codigo = generarCodigoUnico();
        LocalDateTime ahora = LocalDateTime.now();

        CodigoInvitacion invitacion = CodigoInvitacion.builder()
                .grupo(grupo)
                .codigo(codigo)
                .fechaGeneracion(ahora)
                .fechaExpiracion(ahora.plusMinutes(MINUTOS_VALIDEZ_CODIGO))
                .usado(false)
                .build();

        return CodigoInvitacionDTO.from(codigoRepo.save(invitacion));
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{id}/codigo-activo
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public CodigoInvitacionDTO obtenerCodigoActivo(Long idUsuario, Long idGrupo) {
        verificarMiembro(idGrupo, idUsuario);
        return codigoRepo.findTopByGrupo_IdGrupoAndFechaExpiracionAfterOrderByFechaGeneracionDesc(
                        idGrupo, LocalDateTime.now())
                .map(CodigoInvitacionDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un código de invitación activo"));
    }

    /* ---------------------------------------------------------------- *
     *  Helpers
     * ---------------------------------------------------------------- */

    private Usuario buscarUsuario(Long idUsuario) {
        return usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Grupo buscarGrupo(Long idGrupo) {
        return grupoRepo.findById(idGrupo)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
    }

    private MiembroGrupo verificarMiembro(Long idGrupo, Long idUsuario) {
        return miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(idGrupo, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
    }

    private MiembroGrupo verificarCreador(Long idGrupo, Long idUsuario) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        if (miembro.getRol() != RolGrupo.CREADOR) {
            throw new ForbiddenException("Solo el creador puede realizar esta acción");
        }
        return miembro;
    }

    private String generarCodigoUnico() {
        for (int intento = 0; intento < 10; intento++) {
            StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                sb.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            String codigo = sb.toString();
            if (!codigoRepo.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de invitación único");
    }

    private void eliminarGrupoYRelacionados(Long idGrupo) {
        // Orden de borrado respetando las FKs.
        votoRepo.deleteByEncuesta_Grupo_IdGrupo(idGrupo);
        opcionRepo.deleteByEncuesta_Grupo_IdGrupo(idGrupo);
        encuestaRepo.deleteByGrupo_IdGrupo(idGrupo);
        codigoRepo.deleteByGrupo_IdGrupo(idGrupo);
        miembroRepo.deleteByGrupo_IdGrupo(idGrupo);
        grupoRepo.deleteById(idGrupo);
    }
}
