package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Asistencia de un miembro a una actividad del itinerario de grupo
 * ("voy" / "no voy"). Solo tiene sentido sobre items en estado CONFIRMADO —
 * lo valida el service, no esta entidad.
 */
@Entity
@Table(
        name = "asistencia_itinerario_grupo",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_asistencia_item_usuario",
                columnNames = {"item_id", "usuario_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"item", "usuario"})
public class AsistenciaItemGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemItinerarioGrupo item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private boolean asiste;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
