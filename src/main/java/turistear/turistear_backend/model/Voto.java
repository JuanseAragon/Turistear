package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Voto de un miembro dentro de una encuesta.
 */
@Entity
@Table(
        name = "votos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_votos_encuesta_usuario",
                columnNames = {"encuesta_id", "usuario_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"encuesta", "opcion", "usuario"})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "encuesta_id", nullable = false)
    private Encuesta encuesta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionEncuesta opcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_voto", nullable = false)
    private LocalDateTime fechaVoto;
}
