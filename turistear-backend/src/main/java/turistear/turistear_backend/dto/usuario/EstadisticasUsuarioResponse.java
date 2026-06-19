package turistear.turistear_backend.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasUsuarioResponse {
    private List<String> provinciasVisitadas;
    private int totalProvincias;
    private int porcentajeArgentina;
    private int diasTotalesViajados;
    private String provinciaFavorita;
    private String etiquetaPredominante;
}
