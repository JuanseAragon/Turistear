package turistear.turistear_backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItemItinerarioGrupo;

import java.util.Optional;

@Repository
public interface ItemItinerarioGrupoRepository extends JpaRepository<ItemItinerarioGrupo, Long> {

    /** Lookup con ownership: el item debe pertenecer al itinerario de grupo indicado. */
    Optional<ItemItinerarioGrupo> findByIdAndItinerarioGrupo_IdItinerarioGrupo(Long id, Long idItinerarioGrupo);

    /**
     * Lookup con lock pesimista de escritura sobre el item. Se usa en operaciones
     * de asistencia para serializar upserts concurrentes (varios miembros tocando
     * "voy"/"no voy" al mismo tiempo) y evitar violaciones de unique constraint.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ItemItinerarioGrupo> findLockedByIdAndItinerarioGrupo_IdItinerarioGrupo(Long id, Long idItinerarioGrupo);
}
