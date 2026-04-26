package turistear.turistear_backend.service.itinerario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import turistear.turistear_backend.repository.ItinerarioRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItinerarioServiceTest {

    @Mock
    private ItinerarioRepository repository;

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
}
