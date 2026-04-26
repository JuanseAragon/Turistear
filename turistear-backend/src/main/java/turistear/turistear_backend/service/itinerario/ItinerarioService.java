package turistear.turistear_backend.service.itinerario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.repository.ItinerarioRepository;

import java.util.Optional;
import java.util.Set;

@Service
public class ItinerarioService implements ItinerarioServiceInterface {

    private final ItinerarioRepository repository;

    @Autowired
    public ItinerarioService(ItinerarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Itinerario> getItinerarioById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public Set<Itinerario> getPublicItinerarios() {
        return this.repository.findByEsPublicoTrue();
    }

    @Override
    public boolean deleteItinerarioById(Long id) {
        if (!this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public Optional<Itinerario> updateItinerary(Long id, RequestUpdateItinerary request) {
        return this.repository.findById(id).map(itinerario -> {
            if (request.nombre() != null) {
                itinerario.setTitulo(request.nombre());
            }
            if (request.descripcion() != null) {
                itinerario.setDescripcion(request.descripcion());
            }
            if (request.esPublico() != null) {
                itinerario.setEsPublico(request.esPublico());
            }
            return this.repository.save(itinerario);
        });
    }
}
