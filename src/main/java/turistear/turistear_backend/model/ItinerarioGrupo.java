package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Itinerario compartido de un grupo: snapshot del itinerario ganador de una
 * encuesta, propiedad del grupo (no de un usuario individual). El control de
 * acceso real es "soy miembro del grupo" (ver MiembroGrupoRepository), no
 * {@link #creador} — ese campo es solo trazabilidad histórica de quién era
 * el creador del grupo en el momento en que se generó este itinerario; si el
 * creador abandona el grupo más adelante, este campo no se actualiza.
 */
@Entity
@Table(name = "itinerarios_grupo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idItinerarioGrupo")
@ToString(exclude = {"grupo", "creador", "encuestaOrigen", "items"})
public class ItinerarioGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario_grupo")
    private Long idItinerarioGrupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @OneToOne
    @JoinColumn(name = "encuesta_origen_id")
    private Encuesta encuestaOrigen;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provincia provincia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "foto_portada", length = 512)
    private String fotoPortada;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "itinerarioGrupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC, hora ASC")
    @Builder.Default
    private List<ItemItinerarioGrupo> items = new ArrayList<>();
}
