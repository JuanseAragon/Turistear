package turistear.turistear_backend.service.itinerario;

import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.model.Itinerario;

import java.util.Optional;
import java.util.Set;

public interface  ItinerarioServiceInterface {

    public Optional<Itinerario> getItinerarioById(Long id);

    Set<Itinerario> getPublicItinerarios();

    boolean deleteItinerarioById(Long id);

    Optional<Itinerario> updateItinerary(Long id, RequestUpdateItinerary request);
}
