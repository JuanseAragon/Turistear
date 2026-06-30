package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.model.OpcionEncuesta;

/**
 * Opción de una encuesta con su conteo de votos.
 * El propietario solo se expone cuando la encuesta ya finalizó.
 */
public record OpcionEncuestaDTO(
        Long id,
        String tituloSnapshot,
        String fotoPortadaSnapshot,
        Integer cantidadVotos,
        Long propietarioId,
        boolean esGanadora
) {
    public static OpcionEncuestaDTO from(OpcionEncuesta opcion, EstadoEncuesta estado,
                                         long cantidadVotos, boolean esGanadora) {
        if (opcion == null) return null;
        Long propietarioId = null;
        if (estado == EstadoEncuesta.FINALIZADA) {
            propietarioId = opcion.getPropietario().getIdUsuario();
        }
        return new OpcionEncuestaDTO(
                opcion.getId(),
                opcion.getTituloSnapshot(),
                opcion.getFotoPortadaSnapshot(),
                (int) cantidadVotos,
                propietarioId,
                esGanadora
        );
    }
}
