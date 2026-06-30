package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.EstadoItemItinerarioGrupo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Actividad del itinerario de grupo. Nace CONFIRMADO si la agrega el líder
 * (creador del grupo), o PROPUESTO si la agrega cualquier otro miembro hasta
 * que el líder la confirme.
 */
@Entity
@Table(name = "itinerario_grupo_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"itinerarioGrupo", "propuestoPor", "asistencias"})
public class ItemItinerarioGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerario_grupo_id", nullable = false)
    private ItinerarioGrupo itinerarioGrupo;

    @Column(name = "nombre_actividad", nullable = false)
    private String nombreActividad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String localidad;

    private String direccion;

    @Column(nullable = false)
    private Integer dia;

    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoItemItinerarioGrupo estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propuesto_por_id", nullable = false)
    private Usuario propuestoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AsistenciaItemGrupo> asistencias = new ArrayList<>();
}
