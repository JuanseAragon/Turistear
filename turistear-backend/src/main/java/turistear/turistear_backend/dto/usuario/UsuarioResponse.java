package turistear.turistear_backend.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import turistear.turistear_backend.enumerable.TipoTema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private TipoTema tema;
    private String fotoPerfil;
}
