package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.common.ErrorResponse;
import turistear.turistear_backend.dto.favoritos.AgregarFotoItinerarioRequest;
import turistear.turistear_backend.dto.favoritos.CrearItinerarioRequest;
import turistear.turistear_backend.dto.favoritos.FotoItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItemFavoritoRequest;
import turistear.turistear_backend.dto.favoritos.ItemItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioResumenDTO;
import turistear.turistear_backend.dto.favoritos.UpdateItinerarioUsuarioRequest;
import turistear.turistear_backend.security.AuthUtils;
import turistear.turistear_backend.service.ServiceItinerariosUsuario;

import java.util.List;

/**
 * Itinerarios propios del usuario ("Mis Itinerarios"): copias de un
 * template del sistema o creados desde cero, con CRUD sobre sus
 * actividades. <strong>Requiere JWT</strong> — el {@code idUsuario}
 * se extrae del token, nunca como path/query param.
 */
@RestController
@RequestMapping("/itinerarios")
@RequiredArgsConstructor
@Tag(name = "Itinerarios de usuario",
        description = "Copias e itinerarios propios del usuario con CRUD sobre sus actividades")
public class ControllerItinerariosUsuario {

    private final ServiceItinerariosUsuario serviceItinerarios;
    private final AuthUtils authUtils;

    /* ---------------- itinerarios completos ---------------- */

    @PostMapping
    @Operation(summary = "Crear un itinerario propio desde cero")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Itinerario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioUsuarioDTO> crearDesdeCero(
            @Valid @RequestBody CrearItinerarioRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceItinerarios.crearDesdeCero(idUsuario, request));
    }

    @PostMapping("/desde-favorito/{idSistema}")
    @Operation(summary = "Crear una copia a partir de un template guardado en favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Copia creada"),
            @ApiResponse(responseCode = "400", description = "El template no está en tus favoritos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario del sistema no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioUsuarioDTO> crearDesdeFavorito(
            @PathVariable Long idSistema,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceItinerarios.crearDesdeFavorito(idUsuario, idSistema));
    }

    @GetMapping
    @Operation(summary = "Listar mis itinerarios")
    public List<ItinerarioUsuarioResumenDTO> listar(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarios.listarItinerarios(idUsuario);
    }

    @GetMapping("/activo")
    @Operation(summary = "Obtener el itinerario 'activo' (fijado o próximo/en curso)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario activo encontrado"),
            @ApiResponse(responseCode = "404", description = "No hay viajes en curso ni próximos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO obtenerActivo(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarios.obtenerActivo(idUsuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un itinerario (con items)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario encontrado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO obtenerDetalle(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarios.obtenerDetalle(idUsuario, id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fechas de un itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario actualizado"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItinerarioUsuarioRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarios.actualizarFechas(idUsuario, id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Itinerario eliminado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarios.eliminarItinerario(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/completar")
    @Operation(summary = "Marcar un itinerario como completado (viaje realizado)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Itinerario marcado como completado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> completar(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarios.completarItinerario(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    @Operation(summary = "Fijar/desfijar un itinerario como el activo del Home (tachuela)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pin actualizado"),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> togglePin(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarios.togglePin(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- items ---------------- */

    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar una actividad nueva al itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad agregada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItemItinerarioUsuarioDTO> agregarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemFavoritoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceItinerarios.agregarItem(idUsuario, id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Editar una actividad del itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario o item no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItemItinerarioUsuarioDTO actualizarItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemFavoritoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarios.actualizarItem(idUsuario, id, itemId, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Eliminar una actividad del itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actividad eliminada"),
            @ApiResponse(responseCode = "404", description = "Itinerario o item no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarios.eliminarItem(idUsuario, id, itemId);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- fotos ---------------- */

    @PostMapping("/{id}/fotos")
    @Operation(summary = "Agregar una foto al itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto agregada"),
            @ApiResponse(responseCode = "400", description = "URL invalida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FotoItinerarioUsuarioDTO> agregarFoto(
            @PathVariable Long id,
            @Valid @RequestBody AgregarFotoItinerarioRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceItinerarios.agregarFoto(idUsuario, id, request));
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Eliminar una foto del itinerario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto eliminada"),
            @ApiResponse(responseCode = "404", description = "Itinerario o foto no encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarFoto(
            @PathVariable Long id,
            @PathVariable Long fotoId,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarios.eliminarFoto(idUsuario, id, fotoId);
        return ResponseEntity.noContent().build();
    }
}
