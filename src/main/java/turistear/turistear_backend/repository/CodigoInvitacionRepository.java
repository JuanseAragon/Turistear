package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.CodigoInvitacion;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoInvitacionRepository extends JpaRepository<CodigoInvitacion, Long> {

    Optional<CodigoInvitacion> findByCodigo(String codigo);

    Optional<CodigoInvitacion> findTopByGrupo_IdGrupoAndFechaExpiracionAfterOrderByFechaGeneracionDesc(
            Long idGrupo, LocalDateTime ahora);

    boolean existsByCodigo(String codigo);

    void deleteByGrupo_IdGrupo(Long idGrupo);
}
