package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lugares")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lugar")
    private Long idLugar;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String localidad;

    private String direccion; // URL de Google Maps, nullable
}