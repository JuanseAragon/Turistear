package turistear.turistear_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioResumenDTO;
import turistear.turistear_backend.dto.favoritos.ItemFavoritoRequest;
import turistear.turistear_backend.dto.favoritos.ItemItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.UpdateItinerarioUsuarioRequest;
import turistear.turistear_backend.exception.ConflictException;
import turistear.turistear_backend.exception.ResourceNotFoundException;
import turistear.turistear_backend.model.*;
import turistear.turistear_backend.repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de favoritos (copias personales del usuario).
 *
 * Todas las operaciones reciben el {@code idUsuario} autenticado y
 * validan ownership — un usuario no puede leer ni modificar copias
 * de otro. La autenticación se resuelve en la capa controller con
 * {@link turistear.turistear_backend.security.AuthUtils}.
 */
@Service
@RequiredArgsConstructor
public class ServiceFavoritos {

    private final ItinerarioUsuarioRepository favoritoRepo;
    private final ItemItinerarioUsuarioRepository itemRepo;
    private final ItinerarioSistemaRepository sistemaRepo;
    private final UsuarioRepository usuarioRepo;

    /* ---------------------------------------------------------------- *
     *  POST /favoritos/{idSistema}                                     *
     * ---------------------------------------------------------------- */

    /**
     * Crea una copia personal del itinerario del sistema indicado.
     * Es una copia profunda — duplica el cabezal y todos los items con
     * los datos de la actividad copiados (snapshot) para que cambios
     * posteriores en {@code actividades} no afecten la copia.
     *
     * Las fechas se inicializan con las del itinerario del sistema; el
     * usuario las puede editar después por {@code PUT /favoritos/\{id\}}.
     */
    @Transactional
    public ItinerarioUsuarioResumenDTO agregarAFavoritos(Long idUsuario, Long idSistema) {
        if (favoritoRepo.existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(idUsuario, idSistema)) {
            throw new ConflictException("Ya tenés este itinerario en favoritos");
        }

        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        ItinerarioSistema sistema = sistemaRepo.findById(idSistema)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerario del sistema no encontrado: " + idSistema));

        ItinerarioUsuario copia = ItinerarioUsuario.builder()
                .usuario(usuario)
                .itinerarioSistema(sistema)
                .fechaInicio(sistema.getFechaInicio())
                .fechaFin(sistema.getFechaFin())
                .build();

        // Snapshot de items: copiamos los datos de Actividad a columnas
        // propias para que la copia del usuario sea independiente.
        for (ItemItinerarioSistema itemSistema : sistema.getItems()) {
            Actividad a = itemSistema.getActividad();
            ItemItinerarioUsuario itemCopia = ItemItinerarioUsuario.builder()
                    .itinerarioUsuario(copia)
                    .nombreActividad(a.getNombre())
                    .descripcion(a.getDescripcion())
                    .localidad(a.getLocalidad())
                    .direccion(a.getDireccion())
                    .dia(itemSistema.getDia())
                    .hora(itemSistema.getHora())
                    .build();
            copia.getItems().add(itemCopia);
        }

        ItinerarioUsuario guardado = favoritoRepo.save(copia);
        sistema.setLikes(sistema.getLikes() + 1);
        sistemaRepo.save(sistema);


        return ItinerarioUsuarioResumenDTO.from(guardado);
    }

    /* ---------------------------------------------------------------- *
     *  GET /favoritos                                                  *
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public List<ItinerarioUsuarioResumenDTO> listarFavoritos(Long idUsuario) {
        return favoritoRepo.findByUsuario_IdUsuarioOrderByFechaInicioAsc(idUsuario).stream()
                .map(ItinerarioUsuarioResumenDTO::from)
                .toList();
    }

    /* ---------------------------------------------------------------- *
     *  GET /favoritos/activo                                           *
     * ---------------------------------------------------------------- */

    /**
     * Favorito "activo": el de fecha más próxima cuya fechaFin todavía
     * no pasó. Si no hay ninguno futuro/en curso devuelve null
     * (controller lo traduce a 404).
     *
     * <p>TODO: la doc del endpoint menciona "Incluye la próxima
     * actividad del día con su hora". Por ahora devolvemos todos los
     * items y el frontend computa cuál es la próxima — si en algún
     * sprint vale la pena moverlo al backend agregamos un campo
     * dedicado {@code proximaActividad}.
     */
    @Transactional(readOnly = true)
    public ItinerarioUsuarioDTO obtenerActivo(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        // Prioridad 1: el favorito fijado con la tachuela, siempre que su
        // viaje siga vigente (fechaFin >= hoy). Si ya venció, no cuenta.
        // Prioridad 2 (fallback): el de fecha más próxima que no terminó,
        // que es el comportamiento histórico cuando no hay ninguno fijado.
        ItinerarioUsuario activo = favoritoRepo
                .findByUsuario_IdUsuarioAndEsPinnedTrueAndFechaFinGreaterThanEqual(idUsuario, hoy)
                .or(() -> favoritoRepo
                        .findFirstByUsuario_IdUsuarioAndFechaFinGreaterThanEqualOrderByFechaInicioAsc(
                                idUsuario, hoy))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No tenés ningún viaje en curso ni próximo"));
        return ItinerarioUsuarioDTO.from(activo);
    }

    /* ---------------------------------------------------------------- *
     *  GET /favoritos/{id}                                             *
     * ---------------------------------------------------------------- */

    @Transactional(readOnly = true)
    public ItinerarioUsuarioDTO obtenerDetalle(Long idUsuario, Long idFavorito) {
        ItinerarioUsuario favorito = favoritoRepo.findByIdConItems(idFavorito, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Favorito no encontrado: " + idFavorito));
        return ItinerarioUsuarioDTO.from(favorito);
    }

    /* ---------------------------------------------------------------- *
     *  PUT /favoritos/{id}                                             *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItinerarioUsuarioDTO actualizarFechas(
            Long idUsuario, Long idFavorito, UpdateItinerarioUsuarioRequest request) {

        ItinerarioUsuario favorito = cargarConOwnership(idUsuario, idFavorito);
        favorito.setFechaInicio(request.fechaInicio());
        favorito.setFechaFin(request.fechaFin());
        return ItinerarioUsuarioDTO.from(favoritoRepo.save(favorito));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /favoritos/{id}                                          *
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarFavorito(Long idUsuario, Long idFavorito) {
        ItinerarioUsuario favorito = cargarConOwnership(idUsuario, idFavorito);

        ItinerarioSistema sistema = favorito.getItinerarioSistema();
        if (sistema != null && sistema.getLikes() > 0) {
            sistema.setLikes(sistema.getLikes() - 1);
            sistemaRepo.save(sistema);
        }

        // CASCADE en Supabase + orphanRemoval en JPA limpian sus items.
        favoritoRepo.delete(favorito);
    }

    /* ---------------------------------------------------------------- *
     *  PATCH /favoritos/{id}/pin                                       *
     * ---------------------------------------------------------------- */

    /**
     * Fija o desfija un favorito como el "activo" del Home (la tachuela).
     * Es exclusivo: al fijar uno se desfija el que estuviera fijado. Si
     * el favorito que se toca ya estaba fijado, queda desfijado (toggle).
     */
    @Transactional
    public void togglePin(Long idUsuario, Long idFavorito) {
        ItinerarioUsuario objetivo = cargarConOwnership(idUsuario, idFavorito);

        // Capturamos el estado ANTES de tocar nada: si el objetivo ya
        // estaba fijado, el findBy de abajo devuelve la MISMA instancia
        // (mismo registro en la sesión de Hibernate) y leer isEsPinned()
        // después daría un valor ya modificado.
        boolean objetivoYaEstaba = objetivo.isEsPinned();

        // Solo puede haber uno fijado a la vez: desfijamos el actual (si hay).
        favoritoRepo.findByUsuario_IdUsuarioAndEsPinnedTrue(idUsuario)
                .ifPresent(actual -> actual.setEsPinned(false));

        // Si el objetivo no era el que estaba fijado, lo fijamos. Si lo
        // era, ya quedó en false arriba (toggle off).
        if (!objetivoYaEstaba) {
            objetivo.setEsPinned(true);
        }
        // Sin save() explícito: las entidades están manejadas dentro de la
        // transacción, el flush al cerrar persiste los cambios (dirty checking).
    }

    /* ---------------------------------------------------------------- *
     *  POST /favoritos/{id}/items                                      *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioUsuarioDTO agregarItem(
            Long idUsuario, Long idFavorito, ItemFavoritoRequest request) {

        ItinerarioUsuario favorito = cargarConOwnership(idUsuario, idFavorito);

        ItemItinerarioUsuario item = ItemItinerarioUsuario.builder()
                .itinerarioUsuario(favorito)
                .nombreActividad(request.nombreActividad())
                .descripcion(request.descripcion())
                .localidad(request.localidad())
                .direccion(request.direccion())
                .dia(request.dia())
                .hora(request.hora())
                .build();
        favorito.getItems().add(item);
        // Cascade ALL en la colección guarda el item al persistir el favorito.
        favoritoRepo.save(favorito);
        return ItemItinerarioUsuarioDTO.from(item);
    }

    /* ---------------------------------------------------------------- *
     *  PUT /favoritos/{id}/items/{itemId}                              *
     * ---------------------------------------------------------------- */

    @Transactional
    public ItemItinerarioUsuarioDTO actualizarItem(
            Long idUsuario, Long idFavorito, Long idItem, ItemFavoritoRequest request) {

        // Ownership chain: el item tiene que pertenecer al favorito,
        // y el favorito al usuario autenticado.
        cargarConOwnership(idUsuario, idFavorito);
        ItemItinerarioUsuario item = itemRepo
                .findByIdAndItinerarioUsuario_IdItinerarioUsuario(idItem, idFavorito)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item no encontrado en este favorito: " + idItem));

        item.setNombreActividad(request.nombreActividad());
        item.setDescripcion(request.descripcion());
        item.setLocalidad(request.localidad());
        item.setDireccion(request.direccion());
        item.setDia(request.dia());
        item.setHora(request.hora());
        return ItemItinerarioUsuarioDTO.from(itemRepo.save(item));
    }

    /* ---------------------------------------------------------------- *
     *  DELETE /favoritos/{id}/items/{itemId}                           *
     * ---------------------------------------------------------------- */

    @Transactional
    public void eliminarItem(Long idUsuario, Long idFavorito, Long idItem) {
        cargarConOwnership(idUsuario, idFavorito);
        ItemItinerarioUsuario item = itemRepo
                .findByIdAndItinerarioUsuario_IdItinerarioUsuario(idItem, idFavorito)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item no encontrado en este favorito: " + idItem));
        itemRepo.delete(item);
    }

    /* ---------------------------------------------------------------- *
     *  Helper de ownership                                             *
     * ---------------------------------------------------------------- */

    /**
     * Carga un favorito validando que pertenezca al usuario solicitante.
     * Si no existe o pertenece a otro usuario tira 404 — no distinguimos
     * los dos casos para no filtrar la existencia de IDs ajenos.
     */
    private ItinerarioUsuario cargarConOwnership(Long idUsuario, Long idFavorito) {
        return favoritoRepo
                .findByIdItinerarioUsuarioAndUsuario_IdUsuario(idFavorito, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Favorito no encontrado: " + idFavorito));
    }
}
