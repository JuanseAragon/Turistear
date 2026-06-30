package turistear.turistear_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import turistear.turistear_backend.dto.common.ErrorResponse;
import turistear.turistear_backend.dto.sistema.ItinerarioSistemaResumenDTO;
import turistear.turistear_backend.security.AuthUtils;
import turistear.turistear_backend.service.FavoritoService;

import java.util.List;

/**
 * Favoritos = <strong>bookmarks</strong> de itinerarios del sistema.
 * Guardar un favorito NO crea una copia: solo marca el template.
 * <strong>Requiere JWT</strong> — el {@code idUsuario} sale del token.
 */
@RestController
@RequestMapping("/favoritos")
@RequiredArgsConstructor
@Tag(name = "Favoritos", description = "Bookmarks de itinerarios del sistema (templates guardados)")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final AuthUtils authUtils;

    @PostMapping("/{idSistema}")
    @Operation(summary = "Guardar un itinerario del sistema como favorito (bookmark, idempotente)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito guardado (o ya existía)"),
            @ApiResponse(responseCode = "404", description = "Itinerario del sistema no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> guardar(@PathVariable Long idSistema, Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        favoritoService.guardarFavorito(idUsuario, idSistema);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar mis favoritos (templates del sistema guardados)")
    public List<ItinerarioSistemaResumenDTO> listar(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return favoritoService.listarFavoritos(idUsuario);
    }

    @DeleteMapping("/{idSistema}")
    @Operation(summary = "Quitar un itinerario del sistema de favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito quitado (o no existía)")
    })
    public ResponseEntity<Void> quitar(@PathVariable Long idSistema, Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        favoritoService.quitarFavorito(idUsuario, idSistema);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idSistema}/existe")
    @Operation(summary = "¿Tengo guardado este itinerario del sistema? (para el toggle en Explorar)")
    public ResponseEntity<Boolean> existe(@PathVariable Long idSistema, Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(favoritoService.existeFavorito(idUsuario, idSistema));
    }
}
