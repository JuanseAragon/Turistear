package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.eliminado = false")
    Optional<Usuario> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.eliminado = false")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.favoritos WHERE u.idUsuario = :id")
    Optional<Usuario> findByIdConFavoritos(@Param("id") Long id);
}
