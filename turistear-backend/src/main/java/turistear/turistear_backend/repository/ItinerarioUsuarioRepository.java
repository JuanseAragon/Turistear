package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
     */
    Optional<ItinerarioUsuario> findFirstByUsuario_IdUsuarioAndFechaFinGreaterThanEqualOrderByFechaInicioAsc(
            Long idUsuario, LocalDate hoy);

    /**
     * Variante con JOIN FETCH a items para evitar el N+1 al armar el DTO
     * completo de la copia (endpoint {@code GET /favoritos/{id}}). Incluye
     * el ownership check en la misma query.
     */
    @Query("""
        SELECT iu FROM ItinerarioUsuario iu
        LEFT JOIN FETCH iu.items
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
     */
    Optional<ItinerarioUsuario> findByUsuario_IdUsuarioAndEsPinnedTrueAndFechaFinGreaterThanEqual(
            Long idUsuario, LocalDate hoy);
}
