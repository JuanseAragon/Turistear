package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.ItemItinerarioUsuario;

import java.util.Optional;

@Repository
public interface ItemItinerarioUsuarioRepository extends JpaRepository<ItemItinerarioUsuario, Long> {

    /**
     * Lookup de un item dentro de una copia personal específica. Sirve
     * para validar en PUT/DELETE /favoritos/{id}/items/{itemId} que el
     * item realmente pertenezca a la copia que el cliente referencia
     * en la URL (defensa básica contra ID guessing).
     */
    Optional<ItemItinerarioUsuario> findByIdAndItinerarioUsuario_IdItinerarioUsuario(
            Long id, Long idItinerarioUsuario);
}
