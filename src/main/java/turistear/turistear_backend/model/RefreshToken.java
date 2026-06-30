package turistear.turistear_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Refresh token persistido (stateful). A diferencia del access token (un JWT
 * de corta duración que el filtro valida sin tocar la base), el refresh token
 * es un UUID opaco que vive en la tabla {@code refresh_tokens}. Guardarlo nos
 * permite revocarlo (logout real), rotarlo y detectar reuso.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idRefreshToken")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long idRefreshToken;

    /** Valor opaco (UUID) que recibe el cliente. Es lo que se busca al refrescar. */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /** Usuario dueño del token. LAZY: no necesitamos cargarlo salvo que lo pidamos. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Momento en que el token deja de ser válido (createdAt + 7 días). */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Marca de revocación: una vez true, el token no se puede volver a usar. */
    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Conveniencia: válido = no revocado y no vencido. */
    @Transient
    public boolean isValido() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
