package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.RolGrupo;

import java.time.LocalDateTime;

/**
 * Membresía de un usuario dentro de un grupo.
 */
@Entity
@Table(
        name = "miembros_grupo",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_miembros_grupo_usuario",
                columnNames = {"grupo_id", "usuario_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"grupo", "usuario"})
public class MiembroGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolGrupo rol;

    @Column(name = "fecha_union", nullable = false)
    private LocalDateTime fechaUnion;
}
