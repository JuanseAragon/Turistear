package turistear.turistear_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import turistear.turistear_backend.enumerable.TipoTema;

@Data
public class UpdatePreferenciasRequest {

    @Schema(example = "OSCURO")
    @NotNull(message = "El tema es obligatorio")
    private TipoTema tema;
}
