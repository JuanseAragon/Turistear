package turistear.turistear_backend.dto.favoritos;

import turistear.turistear_backend.model.FotoItinerarioUsuario;

public record FotoItinerarioUsuarioDTO(
        Long id,
        String url,
        Integer orden
) {
    public static FotoItinerarioUsuarioDTO from(FotoItinerarioUsuario foto) {
        if (foto == null) return null;
        return new FotoItinerarioUsuarioDTO(
                foto.getId(),
                foto.getUrl(),
                foto.getOrden()
        );
    }
}
