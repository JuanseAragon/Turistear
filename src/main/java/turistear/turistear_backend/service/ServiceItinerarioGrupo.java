package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.grupo.ItemItinerarioGrupoDTO;
import turistear.turistear_backend.dto.grupo.ItemItinerarioGrupoRequest;
import turistear.turistear_backend.dto.grupo.ItinerarioGrupoDTO;
import turistear.turistear_backend.enumerable.EstadoItemItinerarioGrupo;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ForbiddenException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.AsistenciaItemGrupoRepository;
import turistear.turistear_backend.repository.ItemItinerarioGrupoRepository;
import turistear.turistear_backend.repository.ItinerarioGrupoRepository;
import turistear.turistear_backend.repository.MiembroGrupoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lógica del itinerario compartido de grupo: ver el detalle, proponer
 * actividades (estado propuesto/confirmado), confirmarlas/editarlas/eliminarlas
 * y marcar asistencia por actividad.
 *
 * <p>El itinerario de grupo lo crea automáticamente {@link ServiceEncuesta} al
 * finalizar/desempatar una encuesta. Acá se opera sobre el ya creado.
 *
 * <p>El control de acceso es por <strong>membresía del grupo</strong>: se valida
 * con {@link #verificarMiembro}/{@link #verificarCreador} antes de tocar los
 * repositorios (mismo patrón que {@code ServiceGrupo}/{@code ServiceEncuesta}).
 */
@Service
@RequiredArgsConstructor
public class ServiceItinerarioGrupo {

    private final ItinerarioGrupoRepository itinerarioGrupoRepo;
    private final ItemItinerarioGrupoRepository itemRepo;
    private final AsistenciaItemGrupoRepository asistenciaRepo;
    private final MiembroGrupoRepository miembroRepo;

    /* ---------------------------------------------------------------- *
     *  GET /grupos/{idGrupo}/itinerario
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public ItinerarioGrupoDTO obtenerDetalle(Long idUsuario, Long idGrupo) {
        verificarMiembro(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);
        return armarDetalle(itinerario, idGrupo, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/itinerario/items
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioGrupoDTO proponerItem(Long idUsuario, Long idGrupo, ItemItinerarioGrupoRequest request) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);

        // Si lo agrega el líder queda confirmado directo; cualquier otro miembro
        // lo deja propuesto hasta que el líder lo confirme.
        EstadoItemItinerarioGrupo estado = miembro.getRol() == RolGrupo.CREADOR
                ? EstadoItemItinerarioGrupo.CONFIRMADO
                : EstadoItemItinerarioGrupo.PROPUESTO;

        ItemItinerarioGrupo item = ItemItinerarioGrupo.builder()
                .itinerarioGrupo(itinerario)
                .nombreActividad(request.nombreActividad())
                .descripcion(request.descripcion())
                .localidad(request.localidad())
                .direccion(request.direccion())
                .dia(request.dia())
                .hora(request.hora())
                .estado(estado)
                .propuestoPor(miembro.getUsuario())
                .fechaCreacion(LocalDateTime.now())
                .build();

        itinerario.getItems().add(item);
        expandirDuracionSiHaceFalta(itinerario, request.dia());
        itinerarioGrupoRepo.save(itinerario);

        return armarItemDTO(item, idGrupo, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  PUT /grupos/{idGrupo}/itinerario/items/{idItem}
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioGrupoDTO actualizarItem(Long idUsuario, Long idGrupo, Long idItem,
                                                 ItemItinerarioGrupoRequest request) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);
        ItemItinerarioGrupo item = buscarItem(idItem, itinerario.getIdItinerarioGrupo());

        // Confirmada: solo el líder edita. Propuesta: solo quien la propuso.
        if (item.getEstado() == EstadoItemItinerarioGrupo.CONFIRMADO) {
            if (miembro.getRol() != RolGrupo.CREADOR) {
                throw new ForbiddenException("Solo el líder puede editar una actividad confirmada");
            }
        } else if (!item.getPropuestoPor().getIdUsuario().equals(idUsuario)) {
            throw new ForbiddenException("Solo quien propuso la actividad puede editarla mientras está pendiente");
        }

        item.setNombreActividad(request.nombreActividad());
        item.setDescripcion(request.descripcion());
        item.setLocalidad(request.localidad());
        item.setDireccion(request.direccion());
        item.setDia(request.dia());
        item.setHora(request.hora());
        itemRepo.save(item);

        expandirDuracionSiHaceFalta(itinerario, request.dia());
        itinerarioGrupoRepo.save(itinerario);

        return armarItemDTO(item, idGrupo, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  PATCH /grupos/{idGrupo}/itinerario/items/{idItem}/confirmar
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioGrupoDTO confirmarItem(Long idUsuario, Long idGrupo, Long idItem) {
        verificarCreador(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);
        ItemItinerarioGrupo item = buscarItem(idItem, itinerario.getIdItinerarioGrupo());

        if (item.getEstado() == EstadoItemItinerarioGrupo.CONFIRMADO) {
            throw new BadRequestException("La actividad ya está confirmada");
        }

        item.setEstado(EstadoItemItinerarioGrupo.CONFIRMADO);
        itemRepo.save(item);
        return armarItemDTO(item, idGrupo, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /grupos/{idGrupo}/itinerario/items/{idItem}
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarItem(Long idUsuario, Long idGrupo, Long idItem) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);
        ItemItinerarioGrupo item = buscarItem(idItem, itinerario.getIdItinerarioGrupo());

        // El líder puede eliminar cualquier item (esto cubre "rechazar propuesta").
        // Un miembro no-líder solo puede eliminar su propia propuesta pendiente.
        if (miembro.getRol() != RolGrupo.CREADOR) {
            boolean esPropiaPropuestaPendiente =
                    item.getEstado() == EstadoItemItinerarioGrupo.PROPUESTO
                            && item.getPropuestoPor().getIdUsuario().equals(idUsuario);
            if (!esPropiaPropuestaPendiente) {
                throw new ForbiddenException("No podés eliminar esta actividad");
            }
        }

        itemRepo.delete(item);
    }

    /* ---------------------------------------------------------------- *
     *  POST /grupos/{idGrupo}/itinerario/items/{idItem}/asistencia
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioGrupoDTO togglearAsistencia(Long idUsuario, Long idGrupo, Long idItem, boolean asiste) {
        MiembroGrupo miembro = verificarMiembro(idGrupo, idUsuario);
        ItinerarioGrupo itinerario = obtenerVigente(idGrupo);
        ItemItinerarioGrupo item = buscarItem(idItem, itinerario.getIdItinerarioGrupo());

        if (item.getEstado() != EstadoItemItinerarioGrupo.CONFIRMADO) {
            throw new BadRequestException("No se puede marcar asistencia de una actividad todavía no confirmada");
        }

        // Upsert por (item, usuario), mismo patrón que el voto de una encuesta.
        AsistenciaItemGrupo asistencia = asistenciaRepo
                .findByItem_IdAndUsuario_IdUsuario(idItem, idUsuario)
                .orElseGet(() -> AsistenciaItemGrupo.builder()
                        .item(item)
                        .usuario(miembro.getUsuario())
                        .build());
        asistencia.setAsiste(asiste);
        asistencia.setFechaActualizacion(LocalDateTime.now());
        asistenciaRepo.save(asistencia);

        return armarItemDTO(item, idGrupo, idUsuario);
    }

    /* ---------------------------------------------------------------- *
     *  Helpers
     * ---------------------------------------------------------------- */

    private ItinerarioGrupo obtenerVigente(Long idGrupo) {
        List<ItinerarioGrupo> lista = itinerarioGrupoRepo.findByGrupo_IdGrupoOrderByFechaCreacionDesc(idGrupo);
        if (lista.isEmpty()) {
            throw new ResourceNotFoundException("El grupo todavía no tiene un itinerario compartido");
        }
        return lista.getFirst();
    }

    private ItemItinerarioGrupo buscarItem(Long idItem, Long idItinerarioGrupo) {
        return itemRepo.findByIdAndItinerarioGrupo_IdItinerarioGrupo(idItem, idItinerarioGrupo)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada en este itinerario: " + idItem));
    }

    private ItinerarioGrupoDTO armarDetalle(ItinerarioGrupo itinerario, Long idGrupo, Long idUsuario) {
        List<Usuario> roster = miembroRepo.findByGrupo_IdGrupo(idGrupo).stream()
                .map(MiembroGrupo::getUsuario)
                .toList();

        // 1 sola query para todas las asistencias del itinerario; se agrupan por item.
        Map<Long, List<AsistenciaItemGrupo>> asistenciasPorItem = asistenciaRepo
                .findByItem_ItinerarioGrupo_IdItinerarioGrupo(itinerario.getIdItinerarioGrupo()).stream()
                .collect(Collectors.groupingBy(a -> a.getItem().getId()));

        List<ItemItinerarioGrupoDTO> items = itinerario.getItems().stream()
                .map(item -> ItemItinerarioGrupoDTO.from(
                        item, idUsuario, roster,
                        asistenciasPorItem.getOrDefault(item.getId(), List.of())))
                .toList();

        return ItinerarioGrupoDTO.from(itinerario, items);
    }

    /** Arma el DTO de un único item (recarga roster + asistencias de ese item). */
    private ItemItinerarioGrupoDTO armarItemDTO(ItemItinerarioGrupo item, Long idGrupo, Long idUsuario) {
        List<Usuario> roster = miembroRepo.findByGrupo_IdGrupo(idGrupo).stream()
                .map(MiembroGrupo::getUsuario)
                .toList();
        List<AsistenciaItemGrupo> asistencias = item.getEstado() == EstadoItemItinerarioGrupo.CONFIRMADO
                ? asistenciaRepo.findByItem_Id(item.getId())
                : List.of();
        return ItemItinerarioGrupoDTO.from(item, idUsuario, roster, asistencias);
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

    /**
     * Si una actividad cae en un día más allá de la duración actual, el
     * itinerario "crece": la duración se estira y la fecha de fin se recalcula.
     * Misma lógica que {@code ServiceItinerariosUsuario} para el itinerario individual.
     */
    private void expandirDuracionSiHaceFalta(ItinerarioGrupo itinerario, Integer dia) {
        if (dia == null) return;
        int duracionActual = itinerario.getDuracionDias() == null ? 1 : itinerario.getDuracionDias();
        if (dia > duracionActual) {
            int dias = Math.max(dia, 1);
            itinerario.setDuracionDias(dias);
            itinerario.setFechaFin(itinerario.getFechaInicio().plusDays(dias - 1L));
        }
    }
}
