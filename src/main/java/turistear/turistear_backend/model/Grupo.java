package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Grupo de usuarios que comparten viajes y votan en encuestas.
 */
@Entity
@Table(name = "grupos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idGrupo")
@ToString(exclude = {"creador", "miembros", "encuestas", "codigosInvitacion"})
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long idGrupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "foto_portada", length = 512)
    private String fotoPortada;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MiembroGrupo> miembros = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Encuesta> encuestas = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CodigoInvitacion> codigosInvitacion = new ArrayList<>();
}
