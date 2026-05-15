package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Item de la copia personal del usuario. A diferencia de
 * {@link ItemItinerarioSistema}, no apunta a la tabla
 * {@code actividades}: los datos de la actividad viven embebidos
 * porque el usuario tiene CRUD completo (puede crear, editar o
 * borrar actividades dentro de su copia).
 */
@Entity
@Table(name = "itinerario_usuario_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "itinerarioUsuario")
public class ItemItinerarioUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "itinerario_usuario_id", nullable = false)
    private ItinerarioUsuario itinerarioUsuario;

    @Column(name = "nombre_actividad", nullable = false)
    private String nombreActividad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String localidad;

    private String direccion;

    @Column(nullable = false)
    private Integer dia;

    private LocalTime hora;
}
