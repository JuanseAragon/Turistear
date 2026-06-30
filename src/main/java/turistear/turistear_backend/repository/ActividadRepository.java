package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.Actividad;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

}
