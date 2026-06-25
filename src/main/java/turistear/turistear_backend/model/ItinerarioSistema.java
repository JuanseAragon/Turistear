package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Itinerarios precargados por el equipo. No se crean desde la app —
 * son las plantillas base que el usuario explora y eventualmente guarda
 * (copiándolas en {@link ItinerarioUsuario}).
 */
@Entity
@Table(name = "itinerarios_sistema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idItinerario")
@ToString(exclude = {"items", "etiquetas"})
public class ItinerarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario")
    private Long idItinerario;

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

    @Column(name = "foto_portada")
    private String fotoPortada;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer likes = 0;

    @OneToMany(mappedBy = "itinerarioSistema", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC, hora ASC")
    @Builder.Default
    private List<ItemItinerarioSistema> items = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "itinerario_sistema_etiquetas",
            joinColumns = @JoinColumn(name = "itinerario_sistema_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    @Builder.Default
    private Set<Etiqueta> etiquetas = new HashSet<>();
}
