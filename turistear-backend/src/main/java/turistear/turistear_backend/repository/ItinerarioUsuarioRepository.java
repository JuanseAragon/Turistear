package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.ItinerarioUsuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItinerarioUsuarioRepository extends JpaRepository<ItinerarioUsuario, Long> {

    /**
     * Lista todos los itinerarios del usuario, ordenados por fecha de
     * inicio ascendente (primero los próximos). {@code @EntityGraph} trae
     * las etiquetas propias en la misma query para evitar N+1 al armar el
     * ResumenDTO.
     */
    @EntityGraph(attributePaths = {"etiquetas"})
    @Query("""
        SELECT DISTINCT iu FROM ItinerarioUsuario iu
        WHERE iu.usuario.idUsuario = :idUsuario
        ORDER BY iu.fechaInicio ASC
    """)
    List<ItinerarioUsuario> findByUsuario_IdUsuarioOrderByFechaInicioAsc(@Param("idUsuario") Long idUsuario);

    /**
     * Lookup con ownership check: el itinerario con ese id que además
     * pertenezca al usuario indicado. Chequeo obligatorio antes de leer
     * o editar — nunca opera sobre itinerarios de otro usuario.
     */
    Optional<ItinerarioUsuario> findByIdItinerarioUsuarioAndUsuario_IdUsuario(
            Long idItinerarioUsuario, Long idUsuario);

    /**
     * Itinerario "activo" por fecha: el de fecha más próxima cuya
     * {@code fechaFin} todavía no pasó. {@code @EntityGraph} pre-carga
     * items y etiquetas propias. Usado por {@code GET /itinerarios/activo}.
     */
    @EntityGraph(attributePaths = {"etiquetas", "items"})
    Optional<ItinerarioUsuario> findFirstByUsuario_IdUsuarioAndFechaFinGreaterThanEqualOrderByFechaInicioAsc(
            Long idUsuario, LocalDate hoy);

    /**
     * Carga completa para endpoints que devuelven {@code ItinerarioUsuarioDTO}:
     * trae items y etiquetas propias en una query con DISTINCT. Incluye el
     * ownership check. Usado por {@code GET /itinerarios/\{id\}} y {@code PUT}.
     */
    @Query("""
        SELECT DISTINCT iu FROM ItinerarioUsuario iu
        LEFT JOIN FETCH iu.items
        LEFT JOIN FETCH iu.etiquetas
        WHERE iu.idItinerarioUsuario = :id
          AND iu.usuario.idUsuario   = :idUsuario
    """)
    Optional<ItinerarioUsuario> findByIdConItems(
            @Param("id") Long idItinerarioUsuario,
            @Param("idUsuario") Long idUsuario);

    /**
     * Itinerario marcado con la "tachuela", sin filtrar por fecha. Lo usa
     * {@code togglePin} para hallar el fijado actual y desfijarlo. Solo
     * puede haber uno a la vez.
     */
    Optional<ItinerarioUsuario> findByUsuario_IdUsuarioAndEsPinnedTrue(Long idUsuario);

    /**
     * Itinerario fijado con la "tachuela" y todavía vigente
     * ({@code fechaFin >= hoy}). Lo usa {@code obtenerActivo}: el fijado
     * solo cuenta como activo mientras su viaje no haya terminado.
     */
    @EntityGraph(attributePaths = {"etiquetas", "items"})
    Optional<ItinerarioUsuario> findByUsuario_IdUsuarioAndEsPinnedTrueAndFechaFinGreaterThanEqual(
            Long idUsuario, LocalDate hoy);

    /**
     * Elimina el itinerario directamente por id, sin pasar por el ciclo
     * de vida de la entidad. Llamar DESPUÉS de
     * {@code itemRepo.deleteByItinerarioUsuarioId}.
     */
    @Modifying
    @Query("DELETE FROM ItinerarioUsuario iu WHERE iu.idItinerarioUsuario = :id")
    void deleteDirectlyById(@Param("id") Long id);

    /* ---------------------------------------------------------------- *
     *  Estadísticas del mapa de provincias.                            *
     *  Leen de itinerarios_usuario con completado = true; ya NO hacen  *
     *  JOIN al sistema — provincia y etiquetas son propias de la copia.*
     * ---------------------------------------------------------------- */

    @Query("""
        SELECT DISTINCT iu.provincia
        FROM ItinerarioUsuario iu
        WHERE iu.usuario.idUsuario = :idUsuario
          AND iu.completado = true
    """)
    List<Provincia> findProvinciasVisitadas(@Param("idUsuario") Long idUsuario);

    @Query(value = """
        SELECT COALESCE(SUM(CAST(fecha_fin - fecha_inicio AS INTEGER) + 1), 0)
        FROM itinerarios_usuario
        WHERE usuario_id = :idUsuario AND completado = TRUE
    """, nativeQuery = true)
    Long findDiasTotalesViajados(@Param("idUsuario") Long idUsuario);

    @Query("""
        SELECT iu.provincia
        FROM ItinerarioUsuario iu
        WHERE iu.usuario.idUsuario = :idUsuario AND iu.completado = true
        GROUP BY iu.provincia
        ORDER BY COUNT(iu) DESC
    """)
    List<Provincia> findProvinciasRanking(@Param("idUsuario") Long idUsuario);

    @Query(value = """
        SELECT e.nombre
        FROM itinerarios_usuario iu
        JOIN itinerario_usuario_etiquetas iue ON iu.id_itinerario_usuario = iue.itinerario_usuario_id
        JOIN etiquetas e ON iue.etiqueta_id = e.id
        WHERE iu.usuario_id = :idUsuario AND iu.completado = TRUE
        GROUP BY e.nombre
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findEtiquetaPredominante(@Param("idUsuario") Long idUsuario);
}
