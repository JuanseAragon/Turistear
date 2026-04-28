package turistear.turistear_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import turistear.turistear_backend.enumerable.TipoTema;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long idUsuario;
    private String nombre;
    private String email;
    private LocalDate fechaNacimiento;
    private TipoTema tema;
    private String fotoPerfil;
}
