package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import turistear.turistear_backend.enumerable.Provincia;

import java.util.Arrays;
import java.util.List;

/**
 * Endpoint público que expone la lista cerrada de provincias argentinas + CABA.
 * El frontend lo consume una sola vez al iniciar la app y filtra localmente
 * para el autocomplete de búsqueda.
 *
 * Devuelve { name, displayName }:
 *   - name: el valor del enum (ej: "BUENOS_AIRES") — es el que se manda
 *           como query param a /api/comunidad/buscar?provincia=...
 *   - displayName: el nombre legible para mostrar en la UI (ej: "Buenos Aires").
 */
@RestController
@RequestMapping("/provincias")
@Tag(name = "Provincias", description = "Catálogo de provincias argentinas")
public class ProvinciaController {

    @GetMapping
    @Operation(summary = "Listar todas las provincias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido")
    })
    public List<ProvinciaDTO> listarProvincias() {
        return Arrays.stream(Provincia.values())
                .map(p -> new ProvinciaDTO(p.name(), p.getDisplayName()))
                .toList();
    }

    public record ProvinciaDTO(String name, String displayName) {}
}
