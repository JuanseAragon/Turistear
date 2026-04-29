package turistear.turistear_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.Provincia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "itinerarios")
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

    @ManyToOne
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @OneToMany( mappedBy = "itinerario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, hora ASC")
    private List<ItemItinerario> itemItinerarios;

    @ManyToMany(mappedBy = "favoritos")
    private Set<Usuario> usuariosQueLoFavoritearon = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getIdItinerario() {
        return idItinerario;
    }

    public String getTitulo() {
        return titulo;
    }

    public Boolean getEsPublico() {
        return esPublico;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Usuario getCreador() {
        return creador;
    }

    public List<ItemItinerario> getItemItinerarios() {
        return itemItinerarios;
    }

    public Set<Usuario> getUsuariosQueLoFavoritearon() {
        return usuariosQueLoFavoritearon;
    }

    public Provincia getDestino() {
        return destino;
    }

    public void setEsPublico(Boolean esPublico) {
        this.esPublico = esPublico;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public void setIdItinerario(Long idItinerario) {
        this.idItinerario = idItinerario;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDestino(Provincia destino) {
        this.destino = destino;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setItemItinerarios(List<ItemItinerario> itemItinerarios) {
        this.itemItinerarios = itemItinerarios;
    }

    public void setUsuariosQueLoFavoritearon(Set<Usuario> usuariosQueLoFavoritearon) {
        this.usuariosQueLoFavoritearon = usuariosQueLoFavoritearon;
    }
}
