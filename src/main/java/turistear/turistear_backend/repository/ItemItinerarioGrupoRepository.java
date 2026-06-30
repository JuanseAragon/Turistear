package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItemItinerarioGrupo;

import java.util.Optional;

@Repository
public interface ItemItinerarioGrupoRepository extends JpaRepository<ItemItinerarioGrupo, Long> {

    /** Lookup con ownership: el item debe pertenecer al itinerario de grupo indicado. */
    Optional<ItemItinerarioGrupo> findByIdAndItinerarioGrupo_IdItinerarioGrupo(Long id, Long idItinerarioGrupo);
}
