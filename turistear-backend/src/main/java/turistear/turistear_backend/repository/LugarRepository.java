package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import turistear.turistear_backend.model.Lugar;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
}