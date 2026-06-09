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
     * Ranking de itinerarios ordenados por popularidad, leyendo el contador
     * desnormalizado {@code likes} (lo mantiene ServiceFavoritos: +1 al
     * guardar como favorito, -1 al quitarlo). Más simple y barato que contar
     * en vivo las filas de itinerarios_usuario con un JOIN + GROUP BY.
     * Usado por {@code GET /itinerario/explorar?ordenar=favoritos}.
     */
    @Query("""
        SELECT i FROM ItinerarioSistema i
        ORDER BY i.likes DESC
    """)
    List<ItinerarioSistema> findRankingByVecesGuardado();

    /**
     * Ranking filtrado por categorías: itinerarios que tienen al menos
     * una etiqueta del set indicado, ordenados por popularidad según el
     * contador desnormalizado {@code likes}.
     * Usado por {@code GET /itinerario/explorar?categoria=X&ordenar=favoritos}.
     */
    @Query("""
        SELECT i FROM ItinerarioSistema i
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
     */
    @Query("""
        SELECT DISTINCT i FROM ItinerarioSistema i
        JOIN i.etiquetas e
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND e.nombre IN :tags
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
     */
    @Query("""
        SELECT i FROM ItinerarioSistema i
        WHERE (:provincia IS NULL OR i.provincia = :provincia)
          AND (i.fechaFin >= COALESCE(:fechaInicio, i.fechaFin))
          AND (i.fechaInicio <= COALESCE(:fechaFin, i.fechaInicio))
    """)
    List<ItinerarioSistema> buscarPorPreferenciasSinTags(
            @Param("provincia") Provincia provincia,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
