package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.Favorito;

import java.util.List;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /**
     * ¿El usuario ya tiene guardado este template como favorito? Lo usa
     * el service para no duplicar bookmarks (POST idempotente) y para el
     * endpoint {@code GET /favoritos/{idSistema}/existe} del toggle.
     */
    boolean existsByUsuario_IdUsuarioAndItinerarioSistema_IdItinerario(
            Long idUsuario, Long idSistema);

    /**
     * Lista los favoritos del usuario trayendo el itinerario del sistema
     * y sus etiquetas en la misma query (evita N+1 al armar el ResumenDTO).
     * Orden por id descendente: primero los guardados más recientemente.
     */
    @EntityGraph(attributePaths = {"itinerarioSistema", "itinerarioSistema.etiquetas"})
    @Query("SELECT f FROM Favorito f WHERE f.usuario.idUsuario = :idUsuario ORDER BY f.id DESC")
    List<Favorito> findByUsuarioConSistema(@Param("idUsuario") Long idUsuario);

    /**
     * Borra el bookmark de un template para un usuario. Devuelve la
     * cantidad de filas borradas (0 o 1) para que el service sepa si
     * realmente existía y, en ese caso, decremente los likes del sistema.
     */
    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.usuario.idUsuario = :idUsuario AND f.itinerarioSistema.idItinerario = :idSistema")
    int deleteByUsuarioAndSistema(@Param("idUsuario") Long idUsuario, @Param("idSistema") Long idSistema);
}
