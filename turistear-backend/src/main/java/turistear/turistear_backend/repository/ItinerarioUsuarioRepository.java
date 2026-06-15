package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItinerarioUsuarioRepository extends JpaRepository<ItinerarioUsuario, Long> {

    /**
     * Lista todos los favoritos del usuario (sus copias personales).
     * Cada {@code itinerarios_usuario} es un favorito tras dropear la
     * tabla {@code favoritos}. Orden por fecha de inicio ascendente
     * para mostrar primero los próximos viajes.
     */
    @EntityGraph(attributePaths = {"itinerarioSistema", "itinerarioSistema.etiquetas"})
    @Query("""
        SELECT DISTINCT iu FROM ItinerarioUsuario iu
        WHERE iu.usuario.idUsuario = :idUsuario
        ORDER BY iu.fechaInicio ASC
    """)
    List<ItinerarioUsuario> findByUsuario_IdUsuarioOrderByFechaInicioAsc(@Param("idUsuario") Long idUsuario);

    /**
     * Lookup con ownership check: la copia con ese id que además
     * pertenezca al usuario indicado. Es el chequeo obligatorio antes de
     * leer o editar una copia — "solo aplica sobre copias propias del
     * usuario autenticado, nunca sobre itinerarios originales del sistema".
     */
    Optional<ItinerarioUsuario> findByIdItinerarioUsuarioAndUsuario_IdUsuario(
            Long idItinerarioUsuario, Long idUsuario);

    /**
     * Favorito "activo": el que está en curso hoy o el próximo a empezar.
     * <p>
     * Estrategia: descarta los que ya terminaron ({@code fechaFin < hoy})
     * y ordena por fechaInicio ascendente. El primero es:
     * <ul>
     *   <li>el que está en curso ({@code fechaInicio <= hoy <= fechaFin}), o</li>
     *   <li>el próximo viaje ({@code fechaInicio > hoy}).</li>
     * </ul>
     * Si no hay ninguno con {@code fechaFin >= hoy}, devuelve empty.
     * Usado por {@code GET /favoritos/activo} (card "En curso" del Home).
     * <p>
     * {@code @EntityGraph} pre-carga items y etiquetas del sistema para que
     * {@code ItinerarioUsuarioDTO.from()} no dispare lazy loads adicionales.
     * Hibernate aplica el filtrado de {@code findFirst} en memoria al combinar
     * {@code @EntityGraph} con colecciones — el conjunto por usuario es pequeño,
     * por lo que esto es aceptable.
     */
    @EntityGraph(attributePaths = {"itinerarioSistema", "itinerarioSistema.etiquetas", "items"})
    Optional<ItinerarioUsuario> findFirstByUsuario_IdUsuarioAndFechaFinGreaterThanEqualOrderByFechaInicioAsc(
            Long idUsuario, LocalDate hoy);

    /**
     * Carga completa para endpoints que devuelven {@code ItinerarioUsuarioDTO}:
     * trae items (colección de la copia) y las etiquetas del itinerario del
     * sistema en 1 query con DISTINCT. Incluye el ownership check.
     * Usado por {@code GET /favoritos/{id}} y {@code PUT /favoritos/{id}}.
     */
    @Query("""
        SELECT DISTINCT iu FROM ItinerarioUsuario iu
        LEFT JOIN FETCH iu.items
        LEFT JOIN FETCH iu.itinerarioSistema sis
        LEFT JOIN FETCH sis.etiquetas
        WHERE iu.idItinerarioUsuario = :id
          AND iu.usuario.idUsuario   = :idUsuario
    """)
    Optional<ItinerarioUsuario> findByIdConItems(
            @Param("id") Long idItinerarioUsuario,
            @Param("idUsuario") Long idUsuario);

    /**
     * Verifica si el usuario ya guardó como favorito un determinado
     * itinerario del sistema. Sirve para evitar copias duplicadas al
     * {@code POST /favoritos/{idSistema}}.
     */
    boolean existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(
            Long idUsuario, Long idItinerarioSistema);

    /**
     * Favorito marcado con la "tachuela", sin filtrar por fecha. Lo usa
     * {@code togglePin} para encontrar el fijado actual y desfijarlo —
     * tiene que hallarlo aunque su viaje ya haya vencido, para no dejar
     * dos fijados al pinear otro. Solo puede haber uno a la vez.
     */
    Optional<ItinerarioUsuario> findByUsuario_IdUsuarioAndEsPinnedTrue(Long idUsuario);

    /**
     * Favorito fijado con la "tachuela" y todavía vigente
     * ({@code fechaFin >= hoy}). Lo usa {@code obtenerActivo}: el fijado
     * solo cuenta como activo mientras su viaje no haya terminado; si ya
     * venció se ignora y la lógica cae al fallback por fecha más próxima.
     * <p>
     * {@code @EntityGraph} pre-carga items y etiquetas del sistema en la
     * misma query — el resultado es 0 o 1 entidad, por lo que el producto
     * cartesiano de esas dos colecciones está acotado y Hibernate las
     * deduplica correctamente.
     */
    @EntityGraph(attributePaths = {"itinerarioSistema", "itinerarioSistema.etiquetas", "items"})
    Optional<ItinerarioUsuario> findByUsuario_IdUsuarioAndEsPinnedTrueAndFechaFinGreaterThanEqual(
            Long idUsuario, LocalDate hoy);

    /**
     * Elimina el favorito directamente por id, sin pasar por el ciclo de
     * vida de la entidad (no dispara {@code orphanRemoval} ni cascade de JPA).
     * Llamar DESPUÉS de {@code itemRepo.deleteByItinerarioUsuarioId}, que ya
     * limpió los items en bulk — así el delete del padre no necesita cargarlos.
     */
    @Modifying
    @Query("DELETE FROM ItinerarioUsuario iu WHERE iu.idItinerarioUsuario = :id")
    void deleteDirectlyById(@Param("id") Long id);
}
