package turistear.turistear_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import turistear.turistear_backend.enumerable.TipoTema;

@Data
public class UpdatePreferenciasRequest {

    @NotNull(message = "El tema es obligatorio")
    private TipoTema tema;
}
