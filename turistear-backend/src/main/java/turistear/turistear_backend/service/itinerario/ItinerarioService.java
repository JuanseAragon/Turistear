package turistear.turistear_backend.service.itinerario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import turistear.turistear_backend.dto.RequestActivity;
import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.dto.ResponseGeneration;
import turistear.turistear_backend.model.Actividad;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.model.Usuario;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;
import turistear.turistear_backend.service.generacion.AdapterAI;
import turistear.turistear_backend.service.busqueda.AdapterMaps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ItinerarioService implements ItinerarioServiceInterface {

    private final ItinerarioRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final AdapterAI adapterAI;
    private final AdapterMaps adapterMaps;

    @Autowired
    public ItinerarioService(ItinerarioRepository repository, UsuarioRepository usuarioRepository, AdapterAI adapterAI, AdapterMaps adapterMaps) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.adapterAI = adapterAI;
        this.adapterMaps = adapterMaps;
    }

    @Override
    public Optional<Itinerario> getItinerarioById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public List<Itinerario> getPublicItinerarios() {
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

    @Override
    public ResponseGeneration generate(RequestGeneration prompt) {

        ResponseGeneration chatGptResponse = this.adapterAI.generate(prompt);
        
        List<RequestActivity> actividadesParaEnriquecer = chatGptResponse.actividades() != null 
                ? chatGptResponse.actividades() 
                : new ArrayList<>();
                
        List<RequestActivity> actividadesEnriquecidas = this.adapterMaps.searchInfo(actividadesParaEnriquecer);

        // Guardar el itinerario en la base de datos si se provee el id de usuario
        if (prompt.idUsuario() != null) {
            Usuario usuario = this.usuarioRepository.findById(prompt.idUsuario())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

            Itinerario nuevoItinerario = Itinerario.builder()
                    .titulo(chatGptResponse.nombre())
                    .esPublico(false)
                    .descripcion("Itinerario generado automáticamente hacia " + prompt.destiny_location())
                    .usuario(usuario)
                    .build();

            if (chatGptResponse.fecha_inicio() != null) {
                nuevoItinerario.setFechaInicio(LocalDate.ofInstant(chatGptResponse.fecha_inicio().toInstant(), ZoneId.systemDefault()));
            }
            if (chatGptResponse.fecha_final() != null) {
                nuevoItinerario.setFechaFin(LocalDate.ofInstant(chatGptResponse.fecha_final().toInstant(), ZoneId.systemDefault()));
            }

            List<Actividad> listaActividades = new ArrayList<>();
            for (RequestActivity reqAct : actividadesEnriquecidas) {
                Actividad actividadEntity = Actividad.builder()
                        .nombre(reqAct.nombre())
                        .descripcion(reqAct.descripcion())
                        .ubicacion(reqAct.ubicacion())
                        .itinerario(nuevoItinerario)
                        .build();

                if (reqAct.fecha_hora_inicial() != null) {
                    actividadEntity.setFechaHoraInicio(LocalDateTime.ofInstant(reqAct.fecha_hora_inicial().toInstant(), ZoneId.systemDefault()));
                }
                if (reqAct.fecha_hora_final() != null) {
                    actividadEntity.setFechaHoraFinal(LocalDateTime.ofInstant(reqAct.fecha_hora_final().toInstant(), ZoneId.systemDefault()));
                }

                listaActividades.add(actividadEntity);
            }

            nuevoItinerario.setActividades(listaActividades);
            this.repository.save(nuevoItinerario);
        }

        return new ResponseGeneration(
                chatGptResponse.nombre(),
                chatGptResponse.fecha_inicio(),
                chatGptResponse.fecha_final(),
                actividadesEnriquecidas
        );
    }
}
