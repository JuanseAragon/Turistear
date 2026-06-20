package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.FotoItinerarioUsuario;

import java.util.Optional;

@Repository
public interface FotoItinerarioUsuarioRepository extends JpaRepository<FotoItinerarioUsuario, Long> {

    Optional<FotoItinerarioUsuario> findByIdAndItinerarioUsuario_IdItinerarioUsuario(
            Long id, Long idItinerarioUsuario);
}
