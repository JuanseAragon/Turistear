package turistear.turistear_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LugarResponse {

    private Long idLugar;
    private String provincia;
    private String localidad;
    private String direccion;
}