package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Item de un itinerario del sistema: asocia una actividad existente a
 * un itinerario, ordenada por día y hora. Las actividades son
 * reutilizables entre itinerarios.
 */
@Entity
@Table(name = "itinerario_sistema_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"itinerarioSistema", "actividad"})
public class ItemItinerarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerario_sistema_id", nullable = false)
    private ItinerarioSistema itinerarioSistema;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @Column(nullable = false)
    private Integer dia;

    private LocalTime hora;
}
