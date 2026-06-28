package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.model.Encuesta;

/**
 * Vista resumida de una encuesta para listados.
 */
public record EncuestaResumenDTO(
        Long idEncuesta,
        String nombre,
        EstadoEncuesta estado,
        Integer cantidadVotos,
        int totalOpciones,
        String ganadorTitulo,
        String ganadorFotoPortada
) {
    public static EncuestaResumenDTO from(Encuesta encuesta, long cantidadVotos, int totalOpciones) {
        if (encuesta == null) return null;
        String tituloGanador = null;
        String fotoGanador = null;
        if (encuesta.getOpcionGanadora() != null) {
            tituloGanador = encuesta.getOpcionGanadora().getTituloSnapshot();
            fotoGanador = encuesta.getOpcionGanadora().getFotoPortadaSnapshot();
        }
        return new EncuestaResumenDTO(
                encuesta.getIdEncuesta(),
                encuesta.getNombre(),
                encuesta.getEstado(),
                (int) cantidadVotos,
                totalOpciones,
                tituloGanador,
                fotoGanador
        );
    }
}
