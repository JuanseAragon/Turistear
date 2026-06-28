package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Opción propuesta dentro de una encuesta.
 * Guarda un snapshot del itinerario para que no cambie si el original muta.
 */
@Entity
@Table(name = "opciones_encuesta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"encuesta", "propietario"})
public class OpcionEncuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "encuesta_id", nullable = false)
    private Encuesta encuesta;

    @Column(name = "id_itinerario_sistema")
    private Long idItinerarioSistema;

    @Column(name = "id_itinerario_usuario")
    private Long idItinerarioUsuario;

    @Column(name = "titulo_snapshot", nullable = false)
    private String tituloSnapshot;

    @Column(name = "foto_portada_snapshot", length = 512)
    private String fotoPortadaSnapshot;

    @ManyToOne(optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;
}
