package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.Voto;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    List<Voto> findByEncuesta_IdEncuesta(Long idEncuesta);

    long countByOpcion_Id(Long idOpcion);

    long countByEncuesta_IdEncuesta(Long idEncuesta);

    boolean existsByEncuesta_IdEncuestaAndUsuario_IdUsuario(Long idEncuesta, Long idUsuario);

    Optional<Voto> findByEncuesta_IdEncuestaAndUsuario_IdUsuario(Long idEncuesta, Long idUsuario);

    void deleteByEncuesta_Grupo_IdGrupo(Long idGrupo);

    void deleteByEncuesta_IdEncuesta(Long idEncuesta);
}
