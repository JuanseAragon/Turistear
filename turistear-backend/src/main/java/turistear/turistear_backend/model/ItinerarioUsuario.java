package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Copia personal de un itinerario del sistema. Se genera cuando el
 * usuario guarda un itinerario en favoritos. Las fechas se inicializan
 * con las del itinerario del sistema y el usuario puede editarlas.
 *
 * Cada {@code ItinerarioUsuario} representa un favorito del usuario:
 * la tabla {@code favoritos} fue eliminada del schema por ser redundante.
 */
@Entity
@Table(name = "itinerarios_usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idItinerarioUsuario")
@ToString(exclude = {"itinerarioSistema", "usuario", "items"})
public class ItinerarioUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario_usuario")
    private Long idItinerarioUsuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerario_sistema_id", nullable = false)
    private ItinerarioSistema itinerarioSistema;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    /**
     * Marca la "tachuela": el favorito que el usuario eligió como activo
     * para mostrar en el Home. Es exclusivo — solo uno puede estar en
     * true a la vez (lo garantiza {@code ServiceFavoritos.togglePin}).
     * Default false para que los registros existentes no rompan al
     * agregar la columna.
     */
    @Column(name = "es_pinned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean esPinned = false;

    @OneToMany(mappedBy = "itinerarioUsuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC, hora ASC")
    @Builder.Default
    private List<ItemItinerarioUsuario> items = new ArrayList<>();
}
