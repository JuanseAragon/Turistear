package turistear.turistear_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "itinerarios")
@SQLRestriction("eliminado = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idItinerario")
@ToString(exclude = {"creador", "itemItinerarios", "usuariosQueLoFavoritearon"})
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario")
    private Long idItinerario;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    private Provincia destino;

    @Column(name = "es_publico")
    private Boolean esPublico;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "foto_itinerario")
    private String fotoItinerario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @OneToMany( mappedBy = "itinerario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, hora ASC")
    private List<ItemItinerario> itemItinerarios = new ArrayList<>();

    @ManyToMany(mappedBy = "favoritos")
    private Set<Usuario> usuariosQueLoFavoritearon = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
