package turistear.turistear_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turistear.turistear_backend.model.RefreshToken;
import turistear.turistear_backend.model.Usuario;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Igual que findByToken pero trae el usuario en la misma query (evita LazyInitializationException al rotar). */
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.usuario WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUsuario(@Param("token") String token);

    /** Revoca de una sola vez todos los tokens activos de un usuario (logout global, cambio de contraseña). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.usuario = :usuario AND rt.revoked = false")
    void revocarTodosDe(@Param("usuario") Usuario usuario);
}
