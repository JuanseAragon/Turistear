package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ErrorResponse;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.dto.ItinerarioRequest;
import turistear.turistear_backend.dto.RequestUpdateItinerary;
import turistear.turistear_backend.service.ServiceItinerario;


import java.util.Set;

@RestController
@RequestMapping("/itinerario")
@Tag(name = "Itinerarios", description = "Creación, consulta, actualización y eliminación de itinerarios")
public class ControllerItinerary {

    private final ServiceItinerario serviceItinerario;

    public ControllerItinerary(ServiceItinerario serviceItinerario){
        this.serviceItinerario = serviceItinerario;
    }

    @PostMapping
    @Operation(summary = "Crear un itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Itinerario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario creador no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioDTO> crearItinerario(@Valid @RequestBody ItinerarioRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(serviceItinerario.crearItinerario(request));
    }

    @GetMapping("/itinerarios")
    @Operation(summary = "Listar los itinerarios de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Set<ItinerarioDTO> getItinerariosDeUsuario(@RequestParam Long id_usuario) {

        return serviceItinerario.obtenerItinerariosPorUsuario(id_usuario);
    }

    @GetMapping("/itinerarios/favoritos")
    @Operation(summary = "Listar los itinerarios marcados como favoritos por un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Set<ItinerarioDTO> getItinerariosFavoritos(@RequestParam Long id_usuario){
        return serviceItinerario.getItinerariosFavoritos(id_usuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un itinerario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario encontrado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioDTO> obtenerItinerarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItinerario.obtenerItinerarioPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioDTO> actualizarItinerario(
            @PathVariable Long id,
            @Valid @RequestBody RequestUpdateItinerary request) {
        return ResponseEntity.ok(serviceItinerario.actualizarItinerario(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un itinerario (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Itinerario eliminado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarItinerario(@PathVariable Long id) {
        serviceItinerario.eliminarItinerario(id);
        return ResponseEntity.noContent().build();
    }
}
