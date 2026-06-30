package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.OpcionEncuesta;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpcionEncuestaRepository extends JpaRepository<OpcionEncuesta, Long> {

    List<OpcionEncuesta> findByEncuesta_IdEncuesta(Long idEncuesta);

    Optional<OpcionEncuesta> findByIdAndEncuesta_IdEncuesta(Long id, Long idEncuesta);

    long countByEncuesta_IdEncuesta(Long idEncuesta);

    List<OpcionEncuesta> findByEncuesta_Grupo_IdGrupo(Long idGrupo);

    void deleteByEncuesta_Grupo_IdGrupo(Long idGrupo);
}
