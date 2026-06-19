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
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.ItinerarioUsuarioResumenDTO;
import turistear.turistear_backend.dto.favoritos.ItemFavoritoRequest;
import turistear.turistear_backend.dto.favoritos.ItemItinerarioUsuarioDTO;
import turistear.turistear_backend.dto.favoritos.UpdateItinerarioUsuarioRequest;
import turistear.turistear_backend.security.AuthUtils;
import turistear.turistear_backend.service.ServiceFavoritos;

import java.util.List;

/**
 * Endpoints de favoritos / copias personales del usuario. <strong>Todos
 * requieren autenticación JWT</strong> — el {@code idUsuario} se
 * extrae del token vía {@link AuthUtils}, nunca se pasa como path
 * param ni query param.
 */
@RestController
@RequestMapping("/itinerarios")
@RequiredArgsConstructor
@Tag(name = "Itinerarios de usuario",
        description = "Copias personales de itinerarios con CRUD sobre sus actividades")
// NOTA (refactor en curso): los bookmarks pasaron a /favoritos (ver FavoritoController).
// Este controller maneja las copias del usuario y se reescribe/renombra en el Módulo 3.
public class ControllerFavoritos {

    private final ServiceFavoritos serviceFavoritos;
    private final AuthUtils authUtils;

    /* ---------------- copias completas ---------------- */

    @PostMapping("/{idSistema}")
    @Operation(summary = "Agregar un itinerario del sistema a favoritos (crea copia personal)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Copia creada"),
            @ApiResponse(responseCode = "404", description = "Itinerario del sistema no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya tenés este itinerario en favoritos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioUsuarioResumenDTO> agregarAFavoritos(
            @PathVariable Long idSistema,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceFavoritos.agregarAFavoritos(idUsuario, idSistema));
    }

    @GetMapping
    @Operation(summary = "Listar mis favoritos")
    public List<ItinerarioUsuarioResumenDTO> listar(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceFavoritos.listarFavoritos(idUsuario);
    }

    @GetMapping("/activo")
    @Operation(summary = "Obtener el favorito 'activo' (próximo o en curso)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito activo encontrado"),
            @ApiResponse(responseCode = "404", description = "No hay viajes en curso ni próximos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO obtenerActivo(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceFavoritos.obtenerActivo(idUsuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un favorito (con items)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito encontrado"),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO obtenerDetalle(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceFavoritos.obtenerDetalle(idUsuario, id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fechas de un favorito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito actualizado"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItinerarioUsuarioDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItinerarioUsuarioRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceFavoritos.actualizarFechas(idUsuario, id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Quitar un itinerario de favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito eliminado"),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceFavoritos.eliminarFavorito(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/completar")
    @Operation(summary = "Marcar un itinerario como completado (viaje realizado)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Itinerario marcado como completado"),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> completar(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceFavoritos.completarItinerario(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    @Operation(summary = "Fijar/desfijar un favorito como el activo del Home (tachuela)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pin actualizado"),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> togglePin(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceFavoritos.togglePin(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- items ---------------- */

    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar una actividad nueva a la copia personal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad agregada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItemItinerarioUsuarioDTO> agregarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemFavoritoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceFavoritos.agregarItem(idUsuario, id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Editar una actividad de la copia personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Favorito o item no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItemItinerarioUsuarioDTO actualizarItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemFavoritoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceFavoritos.actualizarItem(idUsuario, id, itemId, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Eliminar una actividad de la copia personal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actividad eliminada"),
            @ApiResponse(responseCode = "404", description = "Favorito o item no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceFavoritos.eliminarItem(idUsuario, id, itemId);
        return ResponseEntity.noContent().build();
    }
}
