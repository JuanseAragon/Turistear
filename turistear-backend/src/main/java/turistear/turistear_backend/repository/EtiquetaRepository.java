package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import turistear.turistear_backend.model.Etiqueta;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
}
