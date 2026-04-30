package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.Provincia;

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

    @Enumerated(EnumType.STRING)
    private Provincia provincia;

    @Column(nullable = false)
    private String localidad;

    private String direccion; // URL de Google Maps, nullable
}