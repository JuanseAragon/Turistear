package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Bookmark de un itinerario del sistema. Representa que un usuario
 * marcó un template como favorito — NO es una copia. La copia editable
 * se crea aparte, en {@link ItinerarioUsuario}, vía "Crear copia".
 *
 * Es exclusivo por par (usuario, template): el UNIQUE de la tabla
 * impide guardar dos veces el mismo itinerario del sistema.
 */
@Entity
@Table(name = "favoritos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"usuario", "itinerarioSistema"})
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerario_sistema_id", nullable = false)
    private ItinerarioSistema itinerarioSistema;
}
