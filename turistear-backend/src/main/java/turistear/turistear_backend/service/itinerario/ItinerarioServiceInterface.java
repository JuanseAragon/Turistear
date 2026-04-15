package turistear.turistear_backend.service.itinerario;

import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.dto.ResponseGeneration;
import turistear.turistear_backend.model.Itinerario;

import java.util.List;
import java.util.Optional;

public interface  ItinerarioServiceInterface {

    public Optional<Itinerario> getItinerarioById(Long id);

    List<Itinerario> getPublicItinerarios();

    boolean deleteItinerarioById(Long id);

    ResponseGeneration generate(RequestGeneration prompt);

    Optional<Itinerario> updateItinerary(Long id, RequestUpdateItinerary request);
}
