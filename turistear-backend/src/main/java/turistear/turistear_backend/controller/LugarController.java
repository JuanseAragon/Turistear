package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.ErrorResponse;
import turistear.turistear_backend.dto.LugarRequest;
import turistear.turistear_backend.dto.LugarResponse;
import turistear.turistear_backend.service.LugarService;

import java.util.List;

@RestController
@RequestMapping("/lugares")
@RequiredArgsConstructor
@Tag(name = "Lugares", description = "Gestión de lugares turísticos disponibles")
public class LugarController {

    private final LugarService lugarService;

    // Crea un nuevo lugar con provincia, localidad y dirección (URL Maps)
    @PostMapping
    @Operation(summary = "Crear un lugar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LugarResponse> create(@Valid @RequestBody LugarRequest request) {
        return ResponseEntity.ok(lugarService.create(request));
    }

    // Devuelve todos los lugares registrados (útil para mostrar opciones al crear una actividad)
    @GetMapping
    @Operation(summary = "Listar todos los lugares")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido")
    })
    public ResponseEntity<List<LugarResponse>> getAll() {
        return ResponseEntity.ok(lugarService.getAll());
    }

    // Devuelve un lugar específico por su ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un lugar por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LugarResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lugarService.getById(id));
    }

    // Actualiza los datos de un lugar existente
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un lugar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LugarResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LugarRequest request) {
        return ResponseEntity.ok(lugarService.update(id, request));
    }

    // Elimina un lugar por su ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un lugar")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lugarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
