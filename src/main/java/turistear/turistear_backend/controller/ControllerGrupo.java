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
import turistear.turistear_backend.dto.grupo.*;

import turistear.turistear_backend.security.AuthUtils;
import turistear.turistear_backend.service.ServiceEncuesta;
import turistear.turistear_backend.service.ServiceGrupo;
import turistear.turistear_backend.service.ServiceItinerarioGrupo;

import java.util.List;

/**
 * Endpoints para la gestión de grupos y sus encuestas.
 * Requiere JWT; el {@code idUsuario} se extrae del token.
 */
@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@Tag(name = "Grupos", description = "Creación de grupos, invitaciones, membresía y encuestas")
public class ControllerGrupo {

    private final ServiceGrupo serviceGrupo;
    private final ServiceEncuesta serviceEncuesta;
    private final ServiceItinerarioGrupo serviceItinerarioGrupo;
    private final AuthUtils authUtils;

    /* ---------------- grupos ---------------- */

    @PostMapping
    @Operation(summary = "Crear un nuevo grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GrupoDTO> crearGrupo(
            @Valid @RequestBody CrearGrupoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceGrupo.crearGrupo(idUsuario, request));
    }

    @PostMapping("/unirse/{codigo}")
    @Operation(summary = "Unirse a un grupo con un código de invitación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unido al grupo"),
            @ApiResponse(responseCode = "400", description = "Código expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Código no válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya es miembro",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GrupoDTO> unirseAGrupo(
            @PathVariable String codigo,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceGrupo.unirseAGrupo(idUsuario, codigo));
    }

    @GetMapping
    @Operation(summary = "Listar mis grupos")
    public List<GrupoResumenDTO> listarMisGrupos(Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceGrupo.listarMisGrupos(idUsuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo encontrado"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GrupoDTO> obtenerDetalle(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceGrupo.obtenerDetalle(idUsuario, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GrupoDTO> actualizarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarGrupoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceGrupo.actualizarGrupo(idUsuario, id, request));
    }

    @DeleteMapping("/{id}/salir")
    @Operation(summary = "Salir de un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Saliste del grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> salirDeGrupo(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceGrupo.salirDeGrupo(idUsuario, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/miembros/{idMiembro}")
    @Operation(summary = "Eliminar un miembro del grupo (solo creador)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Miembro eliminado"),
            @ApiResponse(responseCode = "400", description = "No podés eliminarte a vos mismo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Miembro no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarMiembro(
            @PathVariable Long id,
            @PathVariable Long idMiembro,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceGrupo.eliminarMiembro(idUsuario, id, idMiembro);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/codigo")
    @Operation(summary = "Generar código de invitación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Código generado"),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CodigoInvitacionDTO> generarCodigoInvitacion(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceGrupo.generarCodigoInvitacion(idUsuario, id));
    }

    @GetMapping("/{id}/codigo-activo")
    @Operation(summary = "Obtener el código de invitación activo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código activo encontrado"),
            @ApiResponse(responseCode = "404", description = "No hay código activo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CodigoInvitacionDTO> obtenerCodigoActivo(
            @PathVariable Long id,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceGrupo.obtenerCodigoActivo(idUsuario, id));
    }

    /* ---------------- encuestas ---------------- */

    @PostMapping("/{idGrupo}/encuestas")
    @Operation(summary = "Crear una encuesta en el grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Encuesta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya hay una encuesta abierta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EncuestaDTO> crearEncuesta(
            @PathVariable Long idGrupo,
            @Valid @RequestBody CrearEncuestaRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceEncuesta.crearEncuesta(idUsuario, idGrupo, request));
    }

    @GetMapping("/{idGrupo}/encuestas")
    @Operation(summary = "Listar encuestas de un grupo")
    public List<EncuestaResumenDTO> listarEncuestas(
            @PathVariable Long idGrupo,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceEncuesta.listarEncuestas(idUsuario, idGrupo);
    }

    @GetMapping("/{idGrupo}/encuestas/{idEncuesta}")
    @Operation(summary = "Detalle de una encuesta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encuesta encontrada"),
            @ApiResponse(responseCode = "404", description = "Encuesta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EncuestaDTO> obtenerDetalleEncuesta(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.obtenerDetalleEncuesta(idUsuario, idEncuesta));
    }

    @PostMapping("/{idGrupo}/encuestas/{idEncuesta}/votar")
    @Operation(summary = "Votar por una opción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voto registrado"),
            @ApiResponse(responseCode = "400", description = "Encuesta cerrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya votó",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EncuestaDTO> votar(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            @Valid @RequestBody VotarRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.votar(idUsuario, idEncuesta, request.idOpcion()));
    }

    @PostMapping("/{idGrupo}/encuestas/{idEncuesta}/finalizar")
    @Operation(summary = "Finalizar una encuesta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encuesta finalizada"),
            @ApiResponse(responseCode = "400", description = "No se puede finalizar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultadoEncuestaDTO> finalizarEncuesta(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.finalizarEncuesta(idUsuario, idEncuesta));
    }

    @PostMapping("/{idGrupo}/encuestas/{idEncuesta}/desempatar")
    @Operation(summary = "Resolver un empate manualmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empate resuelto"),
            @ApiResponse(responseCode = "400", description = "La encuesta no está en empate",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultadoEncuestaDTO> desempatar(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            @Valid @RequestBody DesempateRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.desempatar(idUsuario, idEncuesta, request.idOpcionGanadora()));
    }

    @PostMapping("/{idGrupo}/encuestas/{idEncuesta}/copiar")
    @Operation(summary = "Copiar el itinerario ganador a mis itinerarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario copiado"),
            @ApiResponse(responseCode = "400", description = "La encuesta no está finalizada o no hay favorito",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioUsuarioDTO> copiarGanador(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.copiarGanador(idUsuario, idEncuesta));
    }

    @DeleteMapping("/{idGrupo}/encuestas/{idEncuesta}")
    @Operation(summary = "Eliminar una encuesta (solo creador)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Encuesta eliminada"),
            @ApiResponse(responseCode = "403", description = "No es el creador de la encuesta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Encuesta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarEncuesta(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceEncuesta.eliminarEncuesta(idUsuario, idEncuesta);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idGrupo}/encuestas/{idEncuesta}/opciones/{idOpcion}/detalle")
    @Operation(summary = "Ver el detalle del itinerario de una opción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle obtenido"),
            @ApiResponse(responseCode = "404", description = "Opción o itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> obtenerDetalleOpcion(
            @PathVariable Long idGrupo,
            @PathVariable Long idEncuesta,
            @PathVariable Long idOpcion,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceEncuesta.obtenerDetalleOpcion(idUsuario, idEncuesta, idOpcion));
    }

    /* ---------------- itinerario compartido de grupo ---------------- */

    @PatchMapping("/{idGrupo}/itinerario/fecha-inicio")
    @Operation(summary = "Cambiar la fecha de inicio del itinerario (solo creador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fecha actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioGrupoDTO> actualizarFechaItinerario(
            @PathVariable Long idGrupo,
            @Valid @RequestBody ActualizarFechaItinerarioGrupoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceItinerarioGrupo.actualizarFechaInicio(idUsuario, idGrupo, request));
    }

    @DeleteMapping("/{idGrupo}/itinerario/dias/{dia}")
    @Operation(summary = "Eliminar un día del itinerario (solo creador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Día eliminado"),
            @ApiResponse(responseCode = "400", description = "Día inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es creador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioGrupoDTO> eliminarDiaItinerario(
            @PathVariable Long idGrupo,
            @PathVariable Integer dia,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceItinerarioGrupo.eliminarDia(idUsuario, idGrupo, dia));
    }

    @GetMapping("/{idGrupo}/itinerario")
    @Operation(summary = "Ver el itinerario compartido del grupo (el del último ganador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itinerario obtenido"),
            @ApiResponse(responseCode = "404", description = "El grupo todavía no tiene itinerario compartido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItinerarioGrupoDTO> obtenerItinerario(
            @PathVariable Long idGrupo,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.ok(serviceItinerarioGrupo.obtenerDetalle(idUsuario, idGrupo));
    }

    @PostMapping("/{idGrupo}/itinerario/items")
    @Operation(summary = "Proponer una actividad (confirmada si la agrega el líder, propuesta si no)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad agregada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo o itinerario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ItemItinerarioGrupoDTO> proponerItem(
            @PathVariable Long idGrupo,
            @Valid @RequestBody ItemItinerarioGrupoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceItinerarioGrupo.proponerItem(idUsuario, idGrupo, request));
    }

    @PutMapping("/{idGrupo}/itinerario/items/{idItem}")
    @Operation(summary = "Editar una actividad (líder si está confirmada; el proponente si sigue propuesta)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No podés editar esta actividad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItemItinerarioGrupoDTO actualizarItem(
            @PathVariable Long idGrupo,
            @PathVariable Long idItem,
            @Valid @RequestBody ItemItinerarioGrupoRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarioGrupo.actualizarItem(idUsuario, idGrupo, idItem, request);
    }

    @PatchMapping("/{idGrupo}/itinerario/items/{idItem}/confirmar")
    @Operation(summary = "Confirmar una actividad propuesta (solo el líder)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad confirmada"),
            @ApiResponse(responseCode = "400", description = "La actividad ya estaba confirmada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo el líder puede confirmar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItemItinerarioGrupoDTO confirmarItem(
            @PathVariable Long idGrupo,
            @PathVariable Long idItem,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarioGrupo.confirmarItem(idUsuario, idGrupo, idItem);
    }

    @DeleteMapping("/{idGrupo}/itinerario/items/{idItem}")
    @Operation(summary = "Eliminar/rechazar una actividad (líder cualquiera; miembro solo su propia propuesta)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actividad eliminada"),
            @ApiResponse(responseCode = "403", description = "No podés eliminar esta actividad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminarItem(
            @PathVariable Long idGrupo,
            @PathVariable Long idItem,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        serviceItinerarioGrupo.eliminarItem(idUsuario, idGrupo, idItem);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idGrupo}/itinerario/items/{idItem}/asistencia")
    @Operation(summary = "Marcar si voy / no voy a una actividad (solo si está confirmada)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asistencia registrada"),
            @ApiResponse(responseCode = "400", description = "La actividad todavía no está confirmada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ItemItinerarioGrupoDTO togglearAsistencia(
            @PathVariable Long idGrupo,
            @PathVariable Long idItem,
            @Valid @RequestBody AsistenciaRequest request,
            Authentication authentication) {
        Long idUsuario = authUtils.getIdUsuarioAutenticado(authentication);
        return serviceItinerarioGrupo.togglearAsistencia(idUsuario, idGrupo, idItem, request.asiste());
    }
}
