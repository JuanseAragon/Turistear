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
import turistear.turistear_backend.enumerable.CategoriaActividad;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.service.ServiceComunidad;

import java.util.Set;

@RestController
@RequestMapping("/api/comunidad")
@Tag(name = "Comunidad", description = "Publicaciones, ranking y búsqueda de itinerarios compartidos")
public class ControllerComunidad {

    private final ServiceComunidad serviceComunidad;

    public ControllerComunidad(ServiceComunidad serviceComunidad) {
        this.serviceComunidad = serviceComunidad;
    }

    // GET /api/comunidad/publicaciones
    @GetMapping("/publicaciones")
    @Operation(summary = "Listar todas las publicaciones de la comunidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido")
    })
    public Set<ItinerarioDTO> listarPublicaciones() {
        return serviceComunidad.listarPublicaciones();
    }

    // GET /api/comunidad/ranking
    @GetMapping("/ranking")
    @Operation(summary = "Obtener el ranking de publicaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking obtenido")
    })
    public Set<ItinerarioDTO> obtenerRanking() {
        return serviceComunidad.obtenerRanking();
    }

    // GET /api/comunidad/buscar?provincia=MENDOZA
    // Búsqueda de publicaciones por provincia (filtra Itinerario.destino).
    // Si no se pasa provincia, devuelve todas las publicaciones.
    @GetMapping("/buscar")
    @Operation(summary = "Buscar publicaciones por provincia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados obtenidos"),
            @ApiResponse(responseCode = "400", description = "Provincia inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Set<ItinerarioDTO> buscarPorRegion(
            @RequestParam(required = false) Provincia provincia) {
        return serviceComunidad.buscarPorRegion(provincia);
    }

    // GET /api/comunidad/categoria?categoria=NATURALEZA
    // Filtro de publicaciones por categoría/etiqueta de actividad.
    @GetMapping("/categoria")
    @Operation(summary = "Buscar publicaciones por categoría de actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados obtenidos"),
            @ApiResponse(responseCode = "400", description = "Categoría inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Set<ItinerarioDTO> buscarPorCategoria(
            @RequestParam CategoriaActividad categoria) {
        return serviceComunidad.buscarPorCategoria(categoria);
    }

    // POST /api/comunidad/publicar/{idItinerario}/{idUsuario}
    @PostMapping("/publicar/{idItinerario}/{idUsuario}")
    @Operation(summary = "Publicar un itinerario en la comunidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario publicado"),
            @ApiResponse(responseCode = "403", description = "Solo el creador puede publicar el itinerario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario o usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void publicarItinerario(
            @PathVariable Long idItinerario,
            @PathVariable Long idUsuario) {
        serviceComunidad.publicarItinerario(idItinerario, idUsuario);
    }

    // DELETE /api/comunidad/publicaciones/{idItinerario}/{idUsuario}
    @DeleteMapping("/publicaciones/{idItinerario}/{idUsuario}")
    @Operation(summary = "Eliminar una publicación de la comunidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación eliminada"),
            @ApiResponse(responseCode = "400", description = "El itinerario no está publicado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo el creador puede despublicar el itinerario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario o usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void eliminarPublicacion(
            @PathVariable Long idItinerario,
            @PathVariable Long idUsuario) {
        serviceComunidad.eliminarPublicacion(idItinerario, idUsuario);
    }

    // GET /api/comunidad/publicaciones/{idItinerario}
    @GetMapping("/publicaciones/{idItinerario}")
    @Operation(summary = "Obtener una publicación por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación encontrada"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioDTO obtenerPublicacionPorId(@PathVariable Long idItinerario) {
        return serviceComunidad.obtenerPublicacionPorId(idItinerario);
    }
}
