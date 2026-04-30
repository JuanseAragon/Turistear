package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.CategoriaActividad;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.model.Itinerario;

import java.util.List;
import java.util.Set;

@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {


    // Lista todos los itinerarios marcados como públicos
    Set<Itinerario> findByEsPublicoTrue();

    // Ranking: publicaciones ordenadas por cantidad de veces favoriteadas (desc)
    @Query("""
        SELECT i FROM Itinerario i
        LEFT JOIN i.usuariosQueLoFavoritearon u
        WHERE i.esPublico = true
        GROUP BY i.idItinerario
        ORDER BY COUNT(u) DESC
    """)
    Set<Itinerario> rankingPublicaciones();

    // Búsqueda de publicaciones por provincia.
    // Filtra directamente sobre Itinerario.destino (enum Provincia).
    // Si la provincia viene null, devuelve todas las publicaciones.
    @Query("""
        SELECT i FROM Itinerario i
        WHERE i.esPublico = true
          AND (:provincia IS NULL OR i.destino = :provincia)
    """)
    Set<Itinerario> buscarPorRegion(@Param("provincia") Provincia provincia);

    // Filtro de publicaciones por categoría (cualquier actividad del itinerario
    // tiene al menos una etiqueta con la categoría indicada).
    @Query("""
        SELECT DISTINCT i FROM Itinerario i
        JOIN i.itemItinerarios item
        JOIN item.actividad a
        JOIN a.tags t
        WHERE i.esPublico = true
          AND t.nombre = :categoria
    """)
    Set<Itinerario> buscarPorCategoria(@Param("categoria") CategoriaActividad categoria);
}
