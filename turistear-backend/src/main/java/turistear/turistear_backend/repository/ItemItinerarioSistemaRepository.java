package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItemItinerarioSistema;

/**
 * Acceso a los items de itinerarios del sistema. Por ahora se accede
 * mayoritariamente vía la colección {@code items} en
 * {@link turistear.turistear_backend.model.ItinerarioSistema}, así que
 * no agregamos queries custom todavía — quedan los métodos heredados de
 * {@link JpaRepository} disponibles por si el equipo de admin necesita
 * CRUD individual sobre items.
 */
@Repository
public interface ItemItinerarioSistemaRepository extends JpaRepository<ItemItinerarioSistema, Long> {
}
