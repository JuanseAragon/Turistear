package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.EstadoEncuesta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encuesta de un grupo para elegir un itinerario entre varias opciones.
 */
@Entity
@Table(name = "encuestas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idEncuesta")
@ToString(exclude = {"grupo", "creador", "opciones", "opcionGanadora"})
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encuesta")
    private Long idEncuesta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEncuesta estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne
    @JoinColumn(name = "opcion_ganadora_id")
    private OpcionEncuesta opcionGanadora;

    @Column(name = "nombre", length = 120)
    private String nombre;

    @OneToMany(mappedBy = "encuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpcionEncuesta> opciones = new ArrayList<>();
}
