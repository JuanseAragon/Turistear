package turistear.turistear_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import turistear.turistear_backend.enumerable.TipoTema;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idUsuario")   // ← solo compara por id
@ToString(exclude = {"mis_itinerarios", "favoritos"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contrasenia;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTema tema;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @OneToMany(mappedBy = "creador")
    private Set<Itinerario> mis_itinerarios = new HashSet<>();


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "itinerario_id"))
    private Set<Itinerario> favoritos = new HashSet<>();

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoTema getTema() {
        return tema;
    }

    public Set<Itinerario> getMis_itinerarios() {
        return mis_itinerarios;
    }

    public Set<Itinerario> getFavoritos() {
        return favoritos;
    }
}
