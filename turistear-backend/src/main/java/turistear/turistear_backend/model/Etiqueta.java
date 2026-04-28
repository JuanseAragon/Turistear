package turistear.turistear_backend.model;

import jakarta.persistence.*;
import turistear.turistear_backend.enumerable.CategoriaActividad;

@Entity
@Table(name = "etiqueta")
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaActividad nombre;
}

