package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.CategoriaItinerario;

@Entity
@Table(name = "etiquetas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private CategoriaItinerario nombre;
}
