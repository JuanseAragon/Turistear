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
 * Itinerario propio del usuario. Es una entidad <strong>independiente</strong>:
 * puede nacer como copia de un itinerario del sistema ("Crear copia" desde
 * favoritos) o crearse desde cero. No guarda referencia a la plantilla de
 * origen — al copiar se hace un snapshot de sus datos (titulo, provincia,
 * etiquetas, items), de modo que cambios posteriores en el sistema no la afectan.
 */
@Entity
@Table(name = "itinerarios_usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idItinerarioUsuario")
@ToString(exclude = {"usuario", "items", "etiquetas"})
public class ItinerarioUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario_usuario")
    private Long idItinerarioUsuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

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

    /**
     * Marca la "tachuela": el itinerario que el usuario eligió como activo
     * para mostrar en el Home. Es exclusivo — solo uno puede estar en true
     * a la vez (lo garantiza {@code ServiceItinerariosUsuario.togglePin}).
     */
    @Column(name = "es_pinned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean esPinned = false;

    /**
     * El usuario marcó este viaje como realizado. Alimenta las
     * estadísticas del mapa (provincias visitadas, días, etc.).
     */
    @Column(name = "completado", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean completado = false;

    @OneToMany(mappedBy = "itinerarioUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC, hora ASC")
    @Builder.Default
    private List<ItemItinerarioUsuario> items = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "itinerario_usuario_etiquetas",
            joinColumns = @JoinColumn(name = "itinerario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    @Builder.Default
    private Set<Etiqueta> etiquetas = new HashSet<>();
}
