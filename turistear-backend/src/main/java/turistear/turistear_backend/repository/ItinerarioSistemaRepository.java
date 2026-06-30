package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.ItinerarioSistema;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ItinerarioSistemaRepository extends JpaRepository<ItinerarioSistema, Long> {

    /**
     * Lista todos los itinerarios del sistema ordenados por popularidad,
     * leyendo el contador desnormalizado {@code likes} (lo mantiene
     * FavoritoService: +1 al guardar como favorito, -1 al quitarlo). Más
     * simple y barato que contar en vivo las filas de itinerarios_usuario
     * con un JOIN + GROUP BY.
     * Usado por {@code GET /itinerario/explorar} (orden por defecto).
     * <p>
     * {@code @EntityGraph} trae las etiquetas en la misma query (LEFT JOIN)
     * para evitar el N+1 al armar el ResumenDTO. {@code DISTINCT} descarta
     * las filas duplicadas que ese JOIN de colección genera.
     */
    @EntityGraph(attributePaths = "etiquetas")
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        ORDER BY i.likes DESC
    """)
    List<ItinerarioSistema> findRankingByVecesGuardado();

    /**
     * Lista los itinerarios de una o más categorías (al menos una etiqueta
     * del set indicado), ordenados por popularidad según el contador
     * desnormalizado {@code likes}.
     * Usado por {@code GET /itinerario/explorar?categoria=X}.
     * <p>
     * El filtro va por {@code EXISTS} (no por JOIN) para no interferir con
     * el {@code @EntityGraph} que trae todas las etiquetas de cada itinerario.
     */
    @EntityGraph(attributePaths = "etiquetas")
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        WHERE EXISTS (SELECT 1 FROM i.etiquetas e WHERE e.nombre IN :categorias)
        ORDER BY i.likes DESC
    """)
    List<ItinerarioSistema> findRankingByVecesGuardadoConCategorias(
            @Param("categorias") Set<CategoriaItinerario> categorias);

    /**
     * Búsqueda por preferencias <strong>con</strong> filtro de tags
     * (al menos una etiqueta del set debe matchear). Provincia y fechas
     * siguen siendo opcionales (null = sin filtro).
     * <p>
     * Sobre las fechas: la doc del schema aclara que el match es por
     * <em>solapamiento</em> (overlap), no contención — si el usuario busca
     * del 1 al 7 de julio puede aparecer un itinerario del 6 al 9.
     * <p>
     * El filtro de tags va por {@code EXISTS} (antes era un JOIN directo):
     * así el {@code @EntityGraph} trae TODAS las etiquetas de cada
     * itinerario sin que el filtro las recorte. Mismos resultados que antes,
     * pero sin el N+1 del ResumenDTO.
     */
    @EntityGraph(attributePaths = "etiquetas")
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND EXISTS (SELECT 1 FROM i.etiquetas e WHERE e.nombre IN :tags)
          AND (i.fechaFin >= COALESCE(:fechaInicio, i.fechaFin))
          AND (i.fechaInicio <= COALESCE(:fechaFin, i.fechaInicio))
    """)
    List<ItinerarioSistema> buscarPorPreferenciasConTags(
            @Param("provincia") Provincia provincia,
            @Param("tags") Set<CategoriaItinerario> tags,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Búsqueda por preferencias <strong>sin</strong> filtro de tags. El
     * service elige qué método llamar según si el set de tags vino vacío
     * o nulo — evita pasarle un IN con colección vacía a Hibernate, que
     * tiene comportamiento ambiguo entre versiones.
     * <p>
     * {@code @EntityGraph} + {@code DISTINCT}: trae las etiquetas en la
     * misma query para evitar el N+1 del ResumenDTO.
     */
    @EntityGraph(attributePaths = "etiquetas")
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND (i.fechaFin >= COALESCE(:fechaInicio, i.fechaFin))
          AND (i.fechaInicio <= COALESCE(:fechaFin, i.fechaInicio))
    """)
    List<ItinerarioSistema> buscarPorPreferenciasSinTags(
            @Param("provincia") Provincia provincia,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Detalle completo de un itinerario del sistema con sus items y la
     * actividad de cada item, traídos en una sola query. Reemplaza al
     * {@code findById} genérico en el endpoint de detalle para evitar el
     * N+1 sobre la tabla actividades (una query por item).
     * <p>
     * Solo fetchea la colección {@code items} (no las etiquetas a la vez)
     * para no generar un producto cartesiano entre dos colecciones; las
     * etiquetas quedan lazy — una sola query extra, al ser un único itinerario.
     */
    @EntityGraph(attributePaths = {"items", "items.actividad"})
    @Query("SELECT i FROM ItinerarioSistema i WHERE i.idItinerario = :id")
    Optional<ItinerarioSistema> findDetalleById(@Param("id") Long id);

    /**
     * Decrementa el contador de likes en una sola query de escritura, sin
     * necesidad de cargar la entidad. El {@code AND i.likes > 0} previene
     * que quede negativo si hubiera inconsistencias de datos.
     * Usado por {@code eliminarFavorito} al quitar una copia personal.
     */
    @Modifying
    @Query("UPDATE ItinerarioSistema i SET i.likes = i.likes - 1 WHERE i.idItinerario = :id AND i.likes > 0")
    void decrementarLikes(@Param("id") Long id);

    /**
     * Incrementa el contador de likes en una sola query de escritura, sin
     * cargar la entidad. Usado por {@code FavoritoService} al guardar un
     * itinerario del sistema como favorito (bookmark).
     */
    @Modifying
    @Query("UPDATE ItinerarioSistema i SET i.likes = i.likes + 1 WHERE i.idItinerario = :id")
    void incrementarLikes(@Param("id") Long id);

    /**
     * Elimina el itinerario del sistema directamente, sin pasar por
     * el cascade de JPA (no carga items ni etiquetas en memoria).
     * Las FKs con CASCADE DELETE en Supabase limpian en cascada:
     * {@code itinerario_sistema_items}, {@code itinerario_sistema_etiquetas}
     * e {@code itinerarios_usuario} con sus items.
     */
    @Modifying
    @Query("DELETE FROM ItinerarioSistema i WHERE i.idItinerario = :id")
    void deleteDirectlyById(@Param("id") Long id);
}
