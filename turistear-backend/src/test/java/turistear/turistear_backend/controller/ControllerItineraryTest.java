package turistear.turistear_backend.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import turistear.turistear_backend.dto.RequestGeneration;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.model.Itinerario;
import turistear.turistear_backend.service.itinerario.ItinerarioService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ControllerItineraryTest {

        private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

        @Mock
    private ItinerarioService itinerarioService;

        @InjectMocks
        private ControllerItinerary controller;

        @Test
        void getItineraryById_returns200_whenItineraryExists() {
                Itinerario itinerario = new Itinerario();
                itinerario.setIdItinerario(5L);
                itinerario.setTitulo("Escapada a Cordoba");
                itinerario.setEsPublico(true);

                when(itinerarioService.getItinerarioById(5L)).thenReturn(Optional.of(itinerario));

                ResponseEntity<?> response = controller.getItineraryById(5L);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());

                Itinerario body = (Itinerario) response.getBody();
                assertEquals(5L, body.getIdItinerario());
                assertEquals("Escapada a Cordoba", body.getTitulo());
                assertTrue(body.getEsPublico());
        }

        @Test
        void getItineraryById_returns404_whenItineraryDoesNotExist() {
                when(itinerarioService.getItinerarioById(99L)).thenReturn(Optional.empty());

                ResponseEntity<?> response = controller.getItineraryById(99L);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void deleteItinerary_returns404_whenServiceCannotDelete() {
                when(itinerarioService.deleteItinerarioById(11L)).thenReturn(false);

                ResponseEntity<?> response = controller.deleteItinerary(11L);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void generateItinerary_propagates404_whenServiceThrowsUserNotFound() {
                when(itinerarioService.generate(any()))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

                RequestGeneration request = new RequestGeneration(
                                "Cordoba",
                                new Date(),
                                new Date(),
                                List.of("naturaleza"),
                                77L
                );

                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> controller.generateItinerary(request));

                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }

        @Test
        void requestGeneration_validationFails_whenRequestIsInvalid() {
                RequestGeneration invalid = new RequestGeneration(null, null, null, List.of(), 77L);

                var violations = VALIDATOR.validate(invalid);

                assertFalse(violations.isEmpty());
        }

        @Test
        void requestUpdate_validationFails_whenNoFieldsToUpdate() {
                RequestUpdateItinerary invalid = new RequestUpdateItinerary(null, null, null);

                var violations = VALIDATOR.validate(invalid);

                assertFalse(violations.isEmpty());
        }

        @Test
        void updateItinerary_returns200_whenServiceUpdatesItinerary() {
                Itinerario updated = new Itinerario();
                updated.setIdItinerario(20L);
                updated.setTitulo("Nuevo titulo");
                updated.setDescripcion("Descripcion actualizada");
                updated.setEsPublico(false);

                when(itinerarioService.updateItinerary(any(), any())).thenReturn(Optional.of(updated));

                RequestUpdateItinerary request = new RequestUpdateItinerary(
                                "Nuevo titulo",
                                "Descripcion actualizada",
                                false
                );

                ResponseEntity<?> response = controller.updateItinerary(20L, request);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                Itinerario body = (Itinerario) response.getBody();
                assertEquals(20L, body.getIdItinerario());
                assertEquals("Nuevo titulo", body.getTitulo());
        }

        @Test
        void updateItinerary_returns404_whenServiceDoesNotFindItinerary() {
                when(itinerarioService.updateItinerary(any(), any())).thenReturn(Optional.empty());

                ResponseEntity<?> response = controller.updateItinerary(20L, new RequestUpdateItinerary("Nuevo titulo", null, null));

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
}
