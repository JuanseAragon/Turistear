package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ErrorResponse;
import turistear.turistear_backend.dto.ItinerarioDTO;
import turistear.turistear_backend.service.ServiceFavoritos;

import java.util.Set;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Gestión de itinerarios favoritos del usuario")
public class ControllerFavoritos {

    private final ServiceFavoritos serviceFavoritos;

    public ControllerFavoritos(ServiceFavoritos serviceFavoritos) {
        this.serviceFavoritos = serviceFavoritos;
    }


    @PostMapping("/{idUsuario}/{idItinerario}")
    @Operation(summary = "Agregar un itinerario a favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agregado a favoritos"),
            @ApiResponse(responseCode = "404", description = "Usuario o itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void agregarAFavoritos(
            @PathVariable Long idUsuario,
            @PathVariable Long idItinerario) {
        serviceFavoritos.agregarAFavoritos(idUsuario, idItinerario);
    }

    // DELETE /api/favoritos/{idUsuario}/{idItinerario}
    @DeleteMapping("/{idUsuario}/{idItinerario}")
    @Operation(summary = "Quitar un itinerario de favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quitado de favoritos"),
            @ApiResponse(responseCode = "404", description = "Usuario o itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void eliminarDeFavoritos(
            @PathVariable Long idUsuario,
            @PathVariable Long idItinerario) {
        serviceFavoritos.eliminarDeFavoritos(idUsuario, idItinerario);
    }

    // GET /api/favoritos/{idUsuario}
    @GetMapping("/{idUsuario}")
    @Operation(summary = "Listar los favoritos de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Set<ItinerarioDTO> obtenerFavoritos(@PathVariable Long idUsuario) {
        return serviceFavoritos.obtenerFavoritos(idUsuario);
    }
}
