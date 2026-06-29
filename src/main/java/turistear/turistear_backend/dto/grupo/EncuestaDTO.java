package turistear.turistear_backend.dto.grupo;

import turistear.turistear_backend.enumerable.EstadoEncuesta;
import turistear.turistear_backend.model.Encuesta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista completa de una encuesta con sus opciones.
 */
public record EncuestaDTO(
        Long idEncuesta,
        String nombre,
        EstadoEncuesta estado,
        Long creadorId,
        String nombreCreador,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCierre,
        List<OpcionEncuestaDTO> opciones,
        OpcionEncuestaDTO opcionGanadora,
        boolean yaVoto,
        boolean puedeFinalizar,
        Long idOpcionVotada,
        int cantidadMiembros
) {
    public static EncuestaDTO from(Encuesta encuesta,
                                    List<OpcionEncuestaDTO> opciones,
                                    OpcionEncuestaDTO opcionGanadora,
                                    boolean yaVoto,
                                    boolean puedeFinalizar,
                                    Long idOpcionVotada,
                                    int cantidadMiembros) {
        if (encuesta == null) return null;
        return new EncuestaDTO(
                encuesta.getIdEncuesta(),
                encuesta.getNombre(),
                encuesta.getEstado(),
                encuesta.getCreador().getIdUsuario(),
                encuesta.getCreador().getNombre(),
                encuesta.getFechaCreacion(),
                encuesta.getFechaCierre(),
                opciones,
                opcionGanadora,
                yaVoto,
                puedeFinalizar,
                idOpcionVotada,
                cantidadMiembros
        );
    }
}
