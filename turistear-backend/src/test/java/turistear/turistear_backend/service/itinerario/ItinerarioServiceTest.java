package turistear.turistear_backend.service.itinerario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import turistear.turistear_backend.dto.RequestActivity;
import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.ResponseGeneration;
import turistear.turistear_backend.repository.ItinerarioRepository;
import turistear.turistear_backend.repository.UsuarioRepository;
import turistear.turistear_backend.service.busqueda.AdapterMaps;
import turistear.turistear_backend.service.generacion.AdapterAI;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class ItinerarioServiceTest {

    @Mock
    private ItinerarioRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdapterAI adapterAI;

    @Mock
    private AdapterMaps adapterMaps;

    @InjectMocks
    private ItinerarioService service;

    @Test
    void deleteItinerarioById_returnsFalse_whenItineraryDoesNotExist() {
        when(repository.existsById(44L)).thenReturn(false);

        boolean deleted = service.deleteItinerarioById(44L);

        assertFalse(deleted);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteItinerarioById_returnsTrue_andDeletes_whenItineraryExists() {
        when(repository.existsById(44L)).thenReturn(true);

        boolean deleted = service.deleteItinerarioById(44L);

        assertTrue(deleted);
        verify(repository).deleteById(44L);
    }

    @Test
    void generate_throws404_whenPromptHasUserIdButUserDoesNotExist() {
        RequestGeneration prompt = new RequestGeneration(
                "Buenos Aires",
                new Date(),
                new Date(),
                List.of("gastronomia"),
                9L
        );

        RequestActivity activity = new RequestActivity(
                "Lugar",
                "Descripcion",
                "Direccion",
                new Date(),
                new Date()
        );

        ResponseGeneration aiResponse = new ResponseGeneration(
                "Viaje",
                new Date(),
                new Date(),
                List.of(activity)
        );

        when(adapterAI.generate(prompt)).thenReturn(aiResponse);
        when(adapterMaps.searchInfo(anyList())).thenReturn(List.of(activity));
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.generate(prompt));

        assertEquals(NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void generate_returnsEnrichedActivities_andDoesNotPersist_whenNoUserIdIsProvided() {
        RequestGeneration prompt = new RequestGeneration(
                "Rosario",
                new Date(),
                new Date(),
                List.of("museos"),
                null
        );

        RequestActivity baseActivity = new RequestActivity(
                null,
                null,
                "Monumento a la Bandera",
                null,
                null
        );
        RequestActivity enrichedActivity = new RequestActivity(
                "Monumento Nacional a la Bandera",
                "Lugar turistico",
                "Santa Fe 581",
                null,
                null
        );

        ResponseGeneration aiResponse = new ResponseGeneration(
                "Viaje Rosario",
                new Date(),
                new Date(),
                List.of(baseActivity)
        );

        when(adapterAI.generate(prompt)).thenReturn(aiResponse);
        when(adapterMaps.searchInfo(List.of(baseActivity))).thenReturn(List.of(enrichedActivity));

        ResponseGeneration result = service.generate(prompt);

        assertEquals("Viaje Rosario", result.nombre());
        assertEquals(1, result.actividades().size());
        assertEquals("Monumento Nacional a la Bandera", result.actividades().get(0).nombre());
        verify(usuarioRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }
}
