package turistear.turistear_backend.dto;

import lombok.Builder;
import lombok.Data;
import turistear.turistear_backend.enumerable.Provincia;

@Data
@Builder
public class LugarResponse {

    private Long idLugar;
    private Provincia provincia;
    private String localidad;
    private String direccion;
}