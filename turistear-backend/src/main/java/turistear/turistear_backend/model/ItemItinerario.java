package turistear.turistear_backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "itinerario_items")
public class ItemItinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "itinerario_id")
    private Itinerario itinerario;

    @ManyToOne
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    private LocalDate fecha;
    private LocalTime hora;

    public Actividad getActividad() {
        return actividad;
    }
}

