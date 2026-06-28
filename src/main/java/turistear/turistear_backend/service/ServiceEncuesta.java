package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.grupo.*;
import turistear.turistear_backend.dto.sistema.ItinerarioSistemaDTO;
import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de encuestas de grupo: creación, votación, finalización,
 * desempate y copia del itinerario ganador.
 */
@Service
@RequiredArgsConstructor
public class ServiceEncuesta {

    private final EncuestaRepository encuestaRepo;
    private final OpcionEncuestaRepository opcionRepo;
    private final VotoRepository votoRepo;
    private final GrupoRepository grupoRepo;
    private final MiembroGrupoRepository miembroRepo;
    private final UsuarioRepository usuarioRepo;
    private final ItinerarioSistemaRepository sistemaRepo;
    private final ItinerarioUsuarioRepository itinerarioUsuarioRepo;
    private final FavoritoRepository favoritoRepo;
    private final ServiceItinerariosUsuario serviceItinerariosUsuario;

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/encuestas
     * ---------------------------------------------------------------- */

    @Transactional
    public EncuestaDTO crearEncuesta(Long idUsuario, Long idGrupo, CrearEncuestaRequest request) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);

        List<CrearEncuestaRequest.OpcionSolicitud> opcionesSolicitud = request.opciones();
        if (opcionesSolicitud == null || opcionesSolicitud.isEmpty()) {
            throw new BadRequestException("La encuesta debe tener al menos una opción");
        }

        Set<String> nuevosKeys = opcionesSolicitud.stream()
                .map(s -> s.idItinerarioSistema() != null
                        ? "S:" + s.idItinerarioSistema()
                        : "U:" + s.idItinerarioUsuario())
                .collect(Collectors.toSet());

        List<OpcionEncuesta> opcionesExistentes = opcionRepo.findByEncuesta_Grupo_IdGrupo(idGrupo);
        for (OpcionEncuesta op : opcionesExistentes) {
            String key = op.getIdItinerarioSistema() != null
                    ? "S:" + op.getIdItinerarioSistema()
                    : "U:" + op.getIdItinerarioUsuario();
            if (nuevosKeys.contains(key)) {
                throw new ConflictException("Ya existe una encuesta de estos itinerarios");
            }
        }

        Grupo grupo = miembro.getGrupo();
        Usuario creador = miembro.getUsuario();
        Encuesta encuesta = Encuesta.builder()
                .grupo(grupo)
                .creador(creador)
                .estado(EstadoEncuesta.ABIERTA)
                .fechaCreacion(LocalDateTime.now())
                .nombre(request.nombre())
                .build();

        for (CrearEncuestaRequest.OpcionSolicitud solicitud : opcionesSolicitud) {
            encuesta.getOpciones().add(construirOpcion(solicitud, encuesta, creador, idUsuario));
        }

        Encuesta guardada = encuestaRepo.save(encuesta);
        return armarEncuestaDTO(guardada, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{idGrupo}/encuestas
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public List<EncuestaResumenDTO> listarEncuestas(Long idUsuario, Long idGrupo) {
        verificarMiembro(idGrupo, idUsuario);
        return encuestaRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(idGrupo).stream()
                .map(e -> EncuestaResumenDTO.from(e,
                        votoRepo.countByEncuesta_IdEncuesta(e.getIdEncuesta()),
                        (int) opcionRepo.countByEncuesta_IdEncuesta(e.getIdEncuesta())))
                .toList();
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{idGrupo}/encuestas/{idEncuesta}
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public EncuestaDTO obtenerDetalleEncuesta(Long idUsuario, Long idEncuesta) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        verificarMiembro(encuesta.getGrupo().getIdGrupo(), idUsuario);
        return armarEncuestaDTO(encuesta, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/encuestas/{idEncuesta}/votar
     * ---------------------------------------------------------------- */

    @Transactional
    public EncuestaDTO votar(Long idUsuario, Long idEncuesta, Long idOpcion) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        if (encuesta.getEstado() != EstadoEncuesta.ABIERTA) {
            throw new BadRequestException("La encuesta no está abierta");
        }

        MiembroGrupo miembro = verificarMiembro(encuesta.getGrupo().getIdGrupo(), idUsuario);
        OpcionEncuesta opcion = opcionRepo.findByIdAndEncuesta_IdEncuesta(idOpcion, idEncuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));

        Optional<Voto> votoExistente = votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(idEncuesta, idUsuario);

        if (votoExistente.isPresent()) {
            Voto voto = votoExistente.get();
            if (voto.getOpcion().getId().equals(idOpcion)) {
                return armarEncuestaDTO(encuesta, idUsuario);
            }
            voto.setOpcion(opcion);
            voto.setFechaVoto(LocalDateTime.now());
            votoRepo.save(voto);
        } else {
            Voto voto = Voto.builder()
                    .encuesta(encuesta)
                    .opcion(opcion)
                    .usuario(miembro.getUsuario())
                    .fechaVoto(LocalDateTime.now())
                    .build();
            votoRepo.save(voto);
        }

        return armarEncuestaDTO(encuesta, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/encuestas/{idEncuesta}/finalizar
     * ---------------------------------------------------------------- */

    @Transactional
    public ResultadoEncuestaDTO finalizarEncuesta(Long idUsuario, Long idEncuesta) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        verificarCreador(encuesta.getGrupo().getIdGrupo(), idUsuario);

        if (encuesta.getEstado() != EstadoEncuesta.ABIERTA) {
            throw new BadRequestException("Solo se puede finalizar una encuesta abierta");
        }

        List<OpcionEncuesta> opciones = opcionRepo.findByEncuesta_IdEncuesta(idEncuesta);
        if (opciones.isEmpty()) {
            throw new BadRequestException("La encuesta no tiene opciones");
        }

        Map<Long, Long> votosPorOpcion = opciones.stream()
                .collect(Collectors.toMap(OpcionEncuesta::getId, o -> votoRepo.countByOpcion_Id(o.getId())));

        long maximo = votosPorOpcion.values().stream().max(Long::compare).orElse(0L);
        if (maximo == 0) {
            throw new BadRequestException("No se puede finalizar una encuesta sin votos");
        }

        List<OpcionEncuesta> ganadoras = opciones.stream()
                .filter(o -> votosPorOpcion.get(o.getId()) == maximo)
                .toList();

        if (ganadoras.size() > 1) {
            encuesta.setEstado(EstadoEncuesta.EMPATE);
            encuesta.setFechaCierre(LocalDateTime.now());
            encuestaRepo.save(encuesta);
            return new ResultadoEncuestaDTO(
                    null,
                    true,
                    ganadoras.stream()
                            .map(o -> OpcionEncuestaDTO.from(o, EstadoEncuesta.EMPATE, votosPorOpcion.get(o.getId()), false))
                            .toList()
            );
        }

        OpcionEncuesta ganadora = ganadoras.getFirst();
        encuesta.setEstado(EstadoEncuesta.FINALIZADA);
        encuesta.setOpcionGanadora(ganadora);
        encuesta.setFechaCierre(LocalDateTime.now());
        encuestaRepo.save(encuesta);

        return new ResultadoEncuestaDTO(
                OpcionEncuestaDTO.from(ganadora, EstadoEncuesta.FINALIZADA, maximo, true),
                false,
                List.of()
        );
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/encuestas/{idEncuesta}/desempatar
     * ---------------------------------------------------------------- */

    @Transactional
    public ResultadoEncuestaDTO desempatar(Long idUsuario, Long idEncuesta, Long idOpcionGanadora) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        verificarCreador(encuesta.getGrupo().getIdGrupo(), idUsuario);

        if (encuesta.getEstado() != EstadoEncuesta.EMPATE) {
            throw new BadRequestException("La encuesta no está en empate");
        }

        OpcionEncuesta ganadora = opcionRepo.findByIdAndEncuesta_IdEncuesta(idOpcionGanadora, idEncuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));

        encuesta.setEstado(EstadoEncuesta.FINALIZADA);
        encuesta.setOpcionGanadora(ganadora);
        encuesta.setFechaCierre(LocalDateTime.now());
        encuestaRepo.save(encuesta);

        long votos = votoRepo.countByOpcion_Id(ganadora.getId());
        return new ResultadoEncuestaDTO(
                OpcionEncuestaDTO.from(ganadora, EstadoEncuesta.FINALIZADA, votos, true),
                false,
                List.of()
        );
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/encuestas/{idEncuesta}/copiar
     * ---------------------------------------------------------------- */

    @Transactional
    public ItinerarioUsuarioDTO copiarGanador(Long idUsuario, Long idEncuesta) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        verificarMiembro(encuesta.getGrupo().getIdGrupo(), idUsuario);

        if (encuesta.getEstado() != EstadoEncuesta.FINALIZADA) {
            throw new BadRequestException("La encuesta no está finalizada");
        }

        OpcionEncuesta ganadora = encuesta.getOpcionGanadora();
        if (ganadora == null) {
            throw new BadRequestException("No hay una opción ganadora");
        }

        if (ganadora.getIdItinerarioSistema() != null) {
            if (!favoritoRepo.existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(
                    idUsuario, ganadora.getIdItinerarioSistema())) {
                throw new BadRequestException("Tenés que tener el itinerario en favoritos para copiarlo");
            }
            return serviceItinerariosUsuario.crearDesdeFavorito(idUsuario, ganadora.getIdItinerarioSistema());
        }

        if (ganadora.getIdItinerarioUsuario() != null) {
            return copiarItinerarioUsuario(idUsuario, ganadora.getIdItinerarioUsuario());
        }

        throw new BadRequestException("La opción ganadora no tiene un itinerario asociado");
    }

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{idGrupo}/encuestas/{idEncuesta}/opciones/{idOpcion}/detalle
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public Object obtenerDetalleOpcion(Long idUsuario, Long idEncuesta, Long idOpcion) {
        Encuesta encuesta = buscarEncuesta(idEncuesta);
        verificarMiembro(encuesta.getGrupo().getIdGrupo(), idUsuario);

        OpcionEncuesta opcion = opcionRepo.findByIdAndEncuesta_IdEncuesta(idOpcion, idEncuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));

        if (opcion.getIdItinerarioSistema() != null) {
            ItinerarioSistema sistema = sistemaRepo.findDetalleById(opcion.getIdItinerarioSistema())
                    .orElseThrow(() -> new ResourceNotFoundException("Itinerario del sistema no encontrado"));
            return ItinerarioSistemaDTO.from(sistema);
        }

        if (opcion.getIdItinerarioUsuario() != null) {
            ItinerarioUsuario origen = itinerarioUsuarioRepo.findByIdItinerarioUsuarioConItems(opcion.getIdItinerarioUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Itinerario de usuario no encontrado"));
            return ItinerarioUsuarioDTO.from(origen);
        }

        throw new BadRequestException("La opción no tiene un itinerario asociado");
    }

    /* ---------------------------------------------------------------- *
     *  Helpers
     * ---------------------------------------------------------------- */

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

    private Encuesta buscarEncuesta(Long idEncuesta) {
        return encuestaRepo.findByIdEncuesta(idEncuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));
    }

    private OpcionEncuesta construirOpcion(CrearEncuestaRequest.OpcionSolicitud solicitud,
                                           Encuesta encuesta, Usuario creador, Long idUsuario) {
        if (solicitud.idItinerarioSistema() != null && solicitud.idItinerarioUsuario() != null) {
            throw new BadRequestException("Una opción solo puede referir a un itinerario del sistema o uno propio, no ambos");
        }
        if (solicitud.idItinerarioSistema() == null && solicitud.idItinerarioUsuario() == null) {
            throw new BadRequestException("Una opción debe referir a un itinerario");
        }

        if (solicitud.idItinerarioSistema() != null) {
            ItinerarioSistema sistema = sistemaRepo.findById(solicitud.idItinerarioSistema())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Itinerario del sistema no encontrado: " + solicitud.idItinerarioSistema()));
            return OpcionEncuesta.builder()
                    .encuesta(encuesta)
                    .idItinerarioSistema(sistema.getIdItinerario())
                    .tituloSnapshot(sistema.getTitulo())
                    .fotoPortadaSnapshot(sistema.getFotoPortada())
                    .propietario(creador)
                    .build();
        }

        ItinerarioUsuario usuarioIti = itinerarioUsuarioRepo
                .findByIdItinerarioUsuarioAndUsuario_IdUsuario(solicitud.idItinerarioUsuario(), idUsuario)
                .orElseThrow(() -> new ForbiddenException(
                        "Solo podés proponer itinerarios que te pertenezcan"));

        return OpcionEncuesta.builder()
                .encuesta(encuesta)
                .idItinerarioUsuario(usuarioIti.getIdItinerarioUsuario())
                .tituloSnapshot(usuarioIti.getTitulo())
                .fotoPortadaSnapshot(usuarioIti.getFotoPortada())
                .propietario(usuarioIti.getUsuario())
                .build();
    }

    private EncuestaDTO armarEncuestaDTO(Encuesta encuesta, Long idUsuario) {
        OpcionEncuesta ganadora = null;
        if (encuesta.getEstado() == EstadoEncuesta.FINALIZADA && encuesta.getOpcionGanadora() != null) {
            ganadora = encuesta.getOpciones().stream()
                    .filter(o -> o.getId().equals(encuesta.getOpcionGanadora().getId()))
                    .findFirst()
                    .orElse(encuesta.getOpcionGanadora());
        }

        final OpcionEncuesta opcionGanadora = ganadora;
        List<OpcionEncuestaDTO> opciones = encuesta.getOpciones().stream()
                .map(o -> OpcionEncuestaDTO.from(o, encuesta.getEstado(),
                        votoRepo.countByOpcion_Id(o.getId()),
                        opcionGanadora != null && opcionGanadora.getId().equals(o.getId())))
                .toList();

        OpcionEncuestaDTO ganadoraDto = opcionGanadora != null
                ? opciones.stream()
                        .filter(o -> o.id().equals(opcionGanadora.getId()))
                        .findFirst()
                        .orElse(null)
                : null;

        Optional<Voto> votoUsuario = votoRepo.findByEncuesta_IdEncuestaAndUsuario_IdUsuario(encuesta.getIdEncuesta(), idUsuario);
        boolean yaVoto = votoUsuario.isPresent();
        Long idOpcionVotada = votoUsuario.map(v -> v.getOpcion().getId()).orElse(null);
        boolean puedeFinalizar = esCreador(encuesta.getGrupo().getIdGrupo(), idUsuario);

        return EncuestaDTO.from(encuesta, opciones, ganadoraDto, yaVoto, puedeFinalizar, idOpcionVotada);
    }

    private boolean esCreador(Long idGrupo, Long idUsuario) {
        return miembroRepo.findByGrupo_IdGrupoAndUsuario_IdUsuario(idGrupo, idUsuario)
                .map(m -> m.getRol() == RolGrupo.CREADOR)
                .orElse(false);
    }

    private ItinerarioUsuarioDTO copiarItinerarioUsuario(Long idUsuario, Long idItinerarioUsuario) {
        Usuario destinatario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ItinerarioUsuario origen = itinerarioUsuarioRepo.findByIdItinerarioUsuarioConItems(idItinerarioUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario de usuario no encontrado"));

        ItinerarioUsuario copia = ItinerarioUsuario.builder()
                .usuario(destinatario)
                .titulo(origen.getTitulo())
                .descripcion(origen.getDescripcion())
                .provincia(origen.getProvincia())
                .fechaInicio(origen.getFechaInicio())
                .fechaFin(origen.getFechaFin())
                .fotoPortada(origen.getFotoPortada())
                .duracionDias(origen.getDuracionDias())
                .esPinned(false)
                .completado(false)
                .build();

        copia.getEtiquetas().addAll(origen.getEtiquetas());

        List<ItemItinerarioUsuario> itemsCopia = new ArrayList<>();
        for (ItemItinerarioUsuario item : origen.getItems()) {
            itemsCopia.add(ItemItinerarioUsuario.builder()
                    .itinerarioUsuario(copia)
                    .nombreActividad(item.getNombreActividad())
                    .descripcion(item.getDescripcion())
                    .localidad(item.getLocalidad())
                    .direccion(item.getDireccion())
                    .dia(item.getDia())
                    .hora(item.getHora())
                    .build());
        }
        copia.getItems().addAll(itemsCopia);

        for (FotoItinerarioUsuario foto : origen.getFotos()) {
            copia.getFotos().add(FotoItinerarioUsuario.builder()
                    .itinerarioUsuario(copia)
                    .url(foto.getUrl())
                    .orden(foto.getOrden())
                    .build());
        }

        return ItinerarioUsuarioDTO.from(itinerarioUsuarioRepo.save(copia));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /grupos/{idGrupo}/encuestas/{idEncuesta}
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarEncuesta(Long idUsuario, Long idEncuesta) {
        Encuesta encuesta = encuestaRepo.findById(idEncuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Encuesta no encontrada"));

        if (!encuesta.getCreador().getIdUsuario().equals(idUsuario)) {
            throw new ForbiddenException("Solo el creador puede eliminar la encuesta");
        }

        votoRepo.deleteByEncuesta_IdEncuesta(idEncuesta);
        encuestaRepo.delete(encuesta);
    }
}
