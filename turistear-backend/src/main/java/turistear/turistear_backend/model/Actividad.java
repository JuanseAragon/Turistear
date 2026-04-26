package turistear.turistear_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "actividades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Long idActividad;

    @ManyToMany
    @JoinTable(
            name = "actividad_etiquetas",
            joinColumns = @JoinColumn(name = "actividad_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    private Set<Etiqueta> tags;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String ubicacion;

//    @Column(name = "fecha_hora_inicio")
//    private LocalDateTime fechaHoraInicio;
//
//    @Column(name = "fecha_hora_final")
//    private LocalDateTime fechaHoraFinal;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_itinerario", nullable = false)
//    @JsonIgnore
//    private Itinerario itinerario;


    public Long getIdActividad() {
        return idActividad;
    }

    public Set<Etiqueta> getTags() {
        return tags;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }
}
