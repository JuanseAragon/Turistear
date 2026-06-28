package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Código de invitación multi-uso para unirse a un grupo.
 */
@Entity
@Table(name = "codigos_invitacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "grupo")
public class CodigoInvitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    @Builder.Default
    private boolean usado = false;
}
