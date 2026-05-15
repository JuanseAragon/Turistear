package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.ItinerarioSistema;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface ItinerarioSistemaRepository extends JpaRepository<ItinerarioSistema, Long> {

    /**
     * Filtro por una o más categorías. Devuelve los itinerarios que tienen
     * al menos una etiqueta del set indicado. Usado por
     * {@code GET /itinerario/explorar?categoria=...}.
     */
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        JOIN i.etiquetas e
        WHERE e.nombre IN :categorias
    """)
    List<ItinerarioSistema> findByCategoriaIn(@Param("categorias") Set<CategoriaItinerario> categorias);

    /**
     * Ranking de itinerarios ordenados por cantidad de veces que fueron
     * guardados como favoritos (cada fila de {@code itinerarios_usuario}
     * cuenta como un guardado). Como Usuario ya no mantiene una colección
     * inversa, contamos cruzando contra ItinerarioUsuario con un join ON.
     * Usado por {@code GET /itinerario/explorar?ordenar=favoritos}.
     */
    @Query("""
        SELECT i FROM ItinerarioSistema i
        LEFT JOIN ItinerarioUsuario iu ON iu.itinerarioSistema = i
        GROUP BY i.idItinerario
        ORDER BY COUNT(iu) DESC
    """)
    List<ItinerarioSistema> findRankingByVecesGuardado();

    /**
     * Búsqueda por preferencias <strong>con</strong> filtro de tags
     * (al menos una etiqueta del set debe matchear). Provincia y fechas
     * siguen siendo opcionales (null = sin filtro).
     * <p>
     * Sobre las fechas: la doc del schema aclara que el match es por
     * <em>solapamiento</em> (overlap), no contención — si el usuario busca
     * del 1 al 7 de julio puede aparecer un itinerario del 6 al 9.
     */
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        JOIN i.etiquetas e
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND e.nombre IN :tags
          AND (:fechaInicio IS NULL OR i.fechaFin >= :fechaInicio)
          AND (:fechaFin    IS NULL OR i.fechaInicio <= :fechaFin)
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
     */
    @Query("""
        SELECT i FROM ItinerarioSistema i
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND (:fechaInicio IS NULL OR i.fechaFin >= :fechaInicio)
          AND (:fechaFin    IS NULL OR i.fechaInicio <= :fechaFin)
    """)
    List<ItinerarioSistema> buscarPorPreferenciasSinTags(
            @Param("provincia") Provincia provincia,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
