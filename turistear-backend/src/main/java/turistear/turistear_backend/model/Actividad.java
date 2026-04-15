package turistear.turistear_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String ubicacion;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_final")
    private LocalDateTime fechaHoraFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_itinerario", nullable = false)
    @JsonIgnore
    private Itinerario itinerario;
}
