package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
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
        GROUP BY i.id
        ORDER BY COUNT(u) DESC
    """)
    Set<Itinerario> rankingPublicaciones();
}
