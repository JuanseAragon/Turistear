package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ErrorResponse;
import turistear.turistear_backend.dto.ItinerarioSistemaDTO;
import turistear.turistear_backend.dto.ItinerarioSistemaResumenDTO;
import turistear.turistear_backend.enumerable.CategoriaItinerario;
import turistear.turistear_backend.enumerable.Provincia;
import turistear.turistear_backend.service.ServiceItinerario;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Endpoints públicos sobre itinerarios precargados del sistema.
 * No requiere autenticación para los GET — la app deja explorar sin
 * loguearse. Las copias del usuario se manejan en {@code /favoritos}.
 */
@RestController
@RequestMapping("/itinerario")
@RequiredArgsConstructor
@Tag(name = "Itinerarios del sistema",
        description = "Catálogo precargado: explorar, buscar y ver detalle de itinerarios")
public class ControllerItinerary {

    private final ServiceItinerario serviceItinerario;

    /**
     * Listado de la sección Explorar.
     *
     * <ul>
     *   <li>Sin parámetros: trae todos los itinerarios del sistema.</li>
     *   <li>Con {@code ?categoria=NATURALEZA&categoria=AVENTURA}: filtra
     *       por tags (matchea con al menos una).</li>
     *   <li>Con {@code ?ordenar=favoritos}: orden por cantidad de veces
     *       guardado como favorito (ranking). Por ahora si se combina
     *       con categoria se ignora el filtro de categoria.</li>
     * </ul>
     */
    @GetMapping("/explorar")
    @Operation(summary = "Listar itinerarios del sistema (con filtros opcionales)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido")
    })
    public List<ItinerarioSistemaResumenDTO> explorar(
            @RequestParam(name = "categoria", required = false) Set<CategoriaItinerario> categorias,
            @RequestParam(name = "ordenar", required = false) String ordenar) {

        boolean ordenarPorFavoritos = "favoritos".equalsIgnoreCase(ordenar);
        return serviceItinerario.explorar(categorias, ordenarPorFavoritos);
    }

    /**
     * Buscar por preferencias. Todos los parámetros son opcionales —
     * lo que viene null se ignora. Las fechas filtran por solapamiento
     * (un itinerario aparece si su rango toca al menos un día del
     * rango pedido).
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar itinerarios por preferencias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados obtenidos")
    })
    public List<ItinerarioSistemaResumenDTO> buscar(
            @RequestParam(required = false) Provincia provincia,
            @RequestParam(name = "tags", required = false) Set<CategoriaItinerario> tags,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {

        return serviceItinerario.buscarPorPreferencias(provincia, tags, fechaInicio, fechaFin);
    }

    /**
     * Detalle completo de un itinerario del sistema — incluye sus
     * items con cada actividad.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un itinerario del sistema por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario encontrado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioSistemaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItinerario.obtenerPorId(id));
    }

    /**
     * Borrado de un itinerario del sistema. CASCADE en Supabase limpia
     * sus items, sus tags y todas las copias de usuario que apunten
     * a este. Por ahora no validamos rol admin — queda pendiente.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un itinerario del sistema (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Itinerario eliminado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        serviceItinerario.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
