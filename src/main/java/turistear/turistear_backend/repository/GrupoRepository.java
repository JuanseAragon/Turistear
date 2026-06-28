package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
}
