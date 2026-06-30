package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.enumerable.RolGrupo;
import turistear.turistear_backend.model.MiembroGrupo;

import java.util.List;
import java.util.Optional;

@Repository
public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Long> {

    Optional<MiembroGrupo> findByGrupo_IdGrupoAndUsuario_IdUsuario(Long idGrupo, Long idUsuario);

    List<MiembroGrupo> findByUsuario_IdUsuario(Long idUsuario);

    List<MiembroGrupo> findByGrupo_IdGrupo(Long idGrupo);

    List<MiembroGrupo> findByGrupo_IdGrupoOrderByFechaUnionAsc(Long idGrupo);

    long countByGrupo_IdGrupo(Long idGrupo);

    boolean existsByGrupo_IdGrupoAndUsuario_IdUsuario(Long idGrupo, Long idUsuario);

    List<MiembroGrupo> findByGrupo_IdGrupoAndRol(Long idGrupo, RolGrupo rol);

    void deleteByGrupo_IdGrupo(Long idGrupo);
}
