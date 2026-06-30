package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.model.Encuesta;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {

    boolean existsByGrupo_IdGrupoAndEstado(Long idGrupo, EstadoEncuesta estado);

    @EntityGraph(attributePaths = {"opciones", "opciones.propietario", "creador"})
    List<Encuesta> findByGrupo_IdGrupoOrderByFechaCreacionDesc(Long idGrupo);

    @EntityGraph(attributePaths = {"opciones", "opciones.propietario", "creador", "grupo"})
    Optional<Encuesta> findByIdEncuesta(Long idEncuesta);

    void deleteByGrupo_IdGrupo(Long idGrupo);
}
