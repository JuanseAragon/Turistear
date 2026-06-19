package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.favoritos.AgregarFotoItinerarioRequest;
import turistear.turistear_backend.dto.favoritos.CrearItinerarioRequest;
import turistear.turistear_backend.dto.favoritos.FotoItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItemFavoritoRequest;
import turistear.turistear_backend.dto.favoritos.ItemItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioResumenDTO;
import turistear.turistear_backend.dto.favoritos.UpdateItinerarioUsuarioRequest;
import turistear.turistear_backend.exception.BadRequestException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lógica de los itinerarios propios del usuario ("Mis Itinerarios").
 * Un itinerario de usuario nace de dos formas: copiando un template del
 * sistema que esté en favoritos ({@link #crearDesdeFavorito}) o desde
 * cero ({@link #crearDesdeCero}). Es independiente: snapshot de datos,
 * sin referencia a la plantilla de origen.
 *
 * Todas las operaciones validan ownership — un usuario nunca toca
 * itinerarios de otro.
 */
@Service
@RequiredArgsConstructor
public class ServiceItinerariosUsuario {

    private static final int MAX_FOTOS_POR_ITINERARIO = 10;

    private final ItinerarioUsuarioRepository itinerarioRepo;
    private final ItemItinerarioUsuarioRepository itemRepo;
    private final ItinerarioSistemaRepository sistemaRepo;
    private final UsuarioRepository usuarioRepo;
    private final FavoritoRepository favoritoRepo;
    private final EtiquetaRepository etiquetaRepo;
    private final FotoItinerarioUsuarioRepository fotoRepo;

    /* ---------------------------------------------------------------- *
     *  POST /itinerarios  — crear desde cero                           *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItinerarioUsuarioDTO crearDesdeCero(Long idUsuario, CrearItinerarioRequest request) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        int duracion = (int) (ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin()) + 1);

        ItinerarioUsuario nuevo = ItinerarioUsuario.builder()
                .usuario(usuario)
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .provincia(request.provincia())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .fotoPortada(request.fotoPortada())
                .duracionDias(duracion)
                .build();

        if (request.etiquetas() != null && !request.etiquetas().isEmpty()) {
            nuevo.getEtiquetas().addAll(etiquetaRepo.findByNombreIn(request.etiquetas()));
        }

        if (request.fotos() != null && request.fotos().size() > MAX_FOTOS_POR_ITINERARIO) {
            throw new BadRequestException(
                    "Un itinerario puede tener como máximo " + MAX_FOTOS_POR_ITINERARIO + " fotos");
        }

        if (request.fotos() != null) {
            for (int orden = 0; orden < request.fotos().size(); orden++) {
                String url = request.fotos().get(orden);
                if (url == null || url.isBlank()) {
                    throw new BadRequestException("Las URLs de las fotos no pueden estar vacías");
                }
                nuevo.getFotos().add(FotoItinerarioUsuario.builder()
                        .itinerarioUsuario(nuevo)
                        .url(url)
                        .orden(orden)
                        .build());
            }
        }

        return ItinerarioUsuarioDTO.from(itinerarioRepo.save(nuevo));
    }

    /* ---------------------------------------------------------------- *
     *  POST /itinerarios/desde-favorito/{idSistema}  — crear copia     *
     * ---------------------------------------------------------------- */

    /**
     * Crea una copia independiente de un template del sistema. Requiere
     * que el usuario tenga ese template guardado en favoritos. Cada
     * llamada genera una copia nueva (no hay límite ni deduplicación):
     * permite tener varias propuestas de un mismo viaje base.
     */
    @Transactional
    public ItinerarioUsuarioDTO crearDesdeFavorito(Long idUsuario, Long idSistema) {
        if (!favoritoRepo.existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(idUsuario, idSistema)) {
            throw new BadRequestException(
                    "Solo podés crear una copia de un itinerario que tengas en favoritos");
        }

        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        // findDetalleById pre-carga items + actividades en 1 JOIN (evita N+1).
        ItinerarioSistema sistema = sistemaRepo.findDetalleById(idSistema)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Itinerario del sistema no encontrado: " + idSistema));

        ItinerarioUsuario copia = ItinerarioUsuario.builder()
                .usuario(usuario)
                .titulo(sistema.getTitulo())
                .descripcion(sistema.getDescripcion())
                .provincia(sistema.getProvincia())
                .fechaInicio(sistema.getFechaInicio())
                .fechaFin(sistema.getFechaFin())
                .fotoPortada(sistema.getFotoPortada())
                .duracionDias(sistema.getDuracionDias())
                .build();

        // Copiamos las etiquetas (asocia las mismas entidades Etiqueta).
        copia.getEtiquetas().addAll(sistema.getEtiquetas());

        // Snapshot de items: copiamos los datos de Actividad a columnas propias
        // para que la copia sea independiente del catálogo del sistema.
        for (ItemItinerarioSistema itemSistema : sistema.getItems()) {
            Actividad actividad = itemSistema.getActividad();
            ItemItinerarioUsuario itemCopia = ItemItinerarioUsuario.builder()
                    .itinerarioUsuario(copia)
                    .nombreActividad(actividad.getNombre())
                    .descripcion(actividad.getDescripcion())
                    .localidad(actividad.getLocalidad())
                    .direccion(actividad.getDireccion())
                    .dia(itemSistema.getDia())
                    .hora(itemSistema.getHora())
                    .build();
            copia.getItems().add(itemCopia);
        }

        return ItinerarioUsuarioDTO.from(itinerarioRepo.save(copia));
    }

    /* ---------------------------------------------------------------- *
     *  GET /itinerarios                                                *
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public List<ItinerarioUsuarioResumenDTO> listarItinerarios(Long idUsuario) {
        return itinerarioRepo.findByUsuario_IdUsuarioOrderByFechaInicioAsc(idUsuario).stream()
                .map(ItinerarioUsuarioResumenDTO::from)
                .toList();
    }

    /* ---------------------------------------------------------------- *
     *  GET /itinerarios/activo                                         *
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public ItinerarioUsuarioDTO obtenerActivo(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        // Prioridad 1: el fijado con la tachuela, si su viaje sigue vigente.
        // Prioridad 2: el de fecha más próxima que no terminó.
        ItinerarioUsuario activo = itinerarioRepo
                .findByUsuario_IdUsuarioAndEsPinnedTrueAndFechaFinGreaterThanEqual(idUsuario, hoy)
                .or(() -> itinerarioRepo
                        .findFirstByUsuario_IdUsuarioAndFechaFinGreaterThanEqualOrderByFechaInicioAsc(
                                idUsuario, hoy))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No tenés ningún viaje en curso ni próximo"));
        return ItinerarioUsuarioDTO.from(activo);
    }

    /* ---------------------------------------------------------------- *
     *  GET /itinerarios/{id}                                           *
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public ItinerarioUsuarioDTO obtenerDetalle(Long idUsuario, Long id) {
        ItinerarioUsuario itinerario = itinerarioRepo.findByIdConItems(id, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado: " + id));
        return ItinerarioUsuarioDTO.from(itinerario);
    }

    /* ---------------------------------------------------------------- *
     *  PUT /itinerarios/{id}  — actualizar fechas                      *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItinerarioUsuarioDTO actualizarFechas(
            Long idUsuario, Long id, UpdateItinerarioUsuarioRequest request) {
        ItinerarioUsuario itinerario = itinerarioRepo.findByIdConItems(id, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado: " + id));
        itinerario.setFechaInicio(request.fechaInicio());
        itinerario.setFechaFin(request.fechaFin());
        return ItinerarioUsuarioDTO.from(itinerarioRepo.save(itinerario));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /itinerarios/{id}                                        *
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarItinerario(Long idUsuario, Long id) {
        cargarConOwnership(idUsuario, id);
        // Bulk DELETE de items antes del padre (las etiquetas de la join
        // se limpian por ON DELETE CASCADE de la FK). Ya no hay likes del
        // sistema que decrementar: el like vive en el favorito (bookmark).
        itemRepo.deleteByItinerarioUsuarioId(id);
        itinerarioRepo.deleteDirectlyById(id);
    }

    /* ---------------------------------------------------------------- *
     *  PATCH /itinerarios/{id}/pin                                     *
     * ---------------------------------------------------------------- */

    @Transactional
    public void togglePin(Long idUsuario, Long id) {
        ItinerarioUsuario objetivo = cargarConOwnership(idUsuario, id);
        boolean objetivoYaEstaba = objetivo.isEsPinned();

        // Solo puede haber uno fijado: desfijamos el actual (si hay).
        itinerarioRepo.findByUsuario_IdUsuarioAndEsPinnedTrue(idUsuario)
                .ifPresent(actual -> actual.setEsPinned(false));

        if (!objetivoYaEstaba) {
            objetivo.setEsPinned(true);
        }
        // Sin save() explícito: dirty checking persiste al cerrar la tx.
    }

    /* ---------------------------------------------------------------- *
     *  PATCH /itinerarios/{id}/completar                               *
     * ---------------------------------------------------------------- */

    @Transactional
    public void completarItinerario(Long idUsuario, Long id) {
        ItinerarioUsuario itinerario = cargarConOwnership(idUsuario, id);
        itinerario.setCompletado(true);
    }

    /* ---------------------------------------------------------------- *
     *  POST /itinerarios/{id}/items                                    *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioUsuarioDTO agregarItem(Long idUsuario, Long id, ItemFavoritoRequest request) {
        ItinerarioUsuario itinerario = cargarConOwnership(idUsuario, id);

        ItemItinerarioUsuario item = ItemItinerarioUsuario.builder()
                .itinerarioUsuario(itinerario)
                .nombreActividad(request.nombreActividad())
                .descripcion(request.descripcion())
                .localidad(request.localidad())
                .direccion(request.direccion())
                .dia(request.dia())
                .hora(request.hora())
                .build();
        itinerario.getItems().add(item);
        itinerarioRepo.save(itinerario);
        return ItemItinerarioUsuarioDTO.from(item);
    }

    /* ---------------------------------------------------------------- *
     *  PUT /itinerarios/{id}/items/{itemId}                            *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioUsuarioDTO actualizarItem(
            Long idUsuario, Long id, Long idItem, ItemFavoritoRequest request) {
        cargarConOwnership(idUsuario, id);
        ItemItinerarioUsuario item = itemRepo
                .findByIdAndItinerarioUsuario_IdItinerarioUsuario(idItem, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item no encontrado en este itinerario: " + idItem));

        item.setNombreActividad(request.nombreActividad());
        item.setDescripcion(request.descripcion());
        item.setLocalidad(request.localidad());
        item.setDireccion(request.direccion());
        item.setDia(request.dia());
        item.setHora(request.hora());
        return ItemItinerarioUsuarioDTO.from(itemRepo.save(item));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /itinerarios/{id}/items/{itemId}                         *
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarItem(Long idUsuario, Long id, Long idItem) {
        cargarConOwnership(idUsuario, id);
        ItemItinerarioUsuario item = itemRepo
                .findByIdAndItinerarioUsuario_IdItinerarioUsuario(idItem, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item no encontrado en este itinerario: " + idItem));
        itemRepo.delete(item);
    }

    /* ---------------------------------------------------------------- *
     *  POST /itinerarios/{id}/fotos                                    *
     * ---------------------------------------------------------------- */

    @Transactional
    public FotoItinerarioUsuarioDTO agregarFoto(
            Long idUsuario, Long id, AgregarFotoItinerarioRequest request) {
        ItinerarioUsuario itinerario = cargarConOwnership(idUsuario, id);
        if (itinerario.getFotos().size() >= MAX_FOTOS_POR_ITINERARIO) {
            throw new BadRequestException(
                    "El itinerario ya alcanzó el máximo de " + MAX_FOTOS_POR_ITINERARIO + " fotos");
        }
        int siguienteOrden = itinerario.getFotos().stream()
                .mapToInt(FotoItinerarioUsuario::getOrden)
                .max()
                .orElse(-1) + 1;

        FotoItinerarioUsuario foto = FotoItinerarioUsuario.builder()
                .itinerarioUsuario(itinerario)
                .url(request.url())
                .orden(siguienteOrden)
                .build();
        itinerario.getFotos().add(foto);
        return FotoItinerarioUsuarioDTO.from(fotoRepo.save(foto));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /itinerarios/{id}/fotos/{fotoId}                         *
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarFoto(Long idUsuario, Long id, Long fotoId) {
        ItinerarioUsuario itinerario = cargarConOwnership(idUsuario, id);
        FotoItinerarioUsuario foto = fotoRepo
                .findByIdAndItinerarioUsuario_IdItinerarioUsuario(fotoId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto no encontrada en este itinerario: " + fotoId));

        if (foto.getUrl().equals(itinerario.getFotoPortada())) {
            String nuevaPortada = itinerario.getFotos().stream()
                    .filter(candidata -> !fotoId.equals(candidata.getId()))
                    .map(FotoItinerarioUsuario::getUrl)
                    .findFirst()
                    .orElse(null);
            itinerario.setFotoPortada(nuevaPortada);
        }

        fotoRepo.delete(foto);
    }

    /* ---------------------------------------------------------------- *
     *  Helper de ownership                                             *
     * ---------------------------------------------------------------- */

    private ItinerarioUsuario cargarConOwnership(Long idUsuario, Long id) {
        return itinerarioRepo
                .findByIdItinerarioUsuarioAndUsuario_IdUsuario(id, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario no encontrado: " + id));
    }
}
