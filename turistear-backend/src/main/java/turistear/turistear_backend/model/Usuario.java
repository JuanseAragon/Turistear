package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.TipoTema;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idUsuario")
@ToString(exclude = {"mis_itinerarios", "favoritos"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contrasenia;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTema tema;

    // TODO: estas relaciones apuntan al viejo Itinerario y se reescribirán
    // en el paso 5 (ItinerarioUsuario + Favorito). Por ahora se dejan para
    // no romper la compilación de los servicios que aún las usan.
    @OneToMany(mappedBy = "creador")
    private Set<Itinerario> mis_itinerarios = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "itinerario_id"))
    private Set<Itinerario> favoritos = new HashSet<>();
}
