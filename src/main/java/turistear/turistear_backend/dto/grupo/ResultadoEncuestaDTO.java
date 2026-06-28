package turistear.turistear_backend.dto.grupo;

import java.util.List;

/**
 * Resultado de una encuesta finalizada o desempatada.
 */
public record ResultadoEncuestaDTO(
        OpcionEncuestaDTO ganador,
        boolean empate,
        List<OpcionEncuestaDTO> opcionesEmpatadas
) {
}
