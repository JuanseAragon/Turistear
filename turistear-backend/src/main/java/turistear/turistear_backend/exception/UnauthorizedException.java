package turistear.turistear_backend.exception;

/**
 * Indica que el cliente no se autenticó o que las credenciales son inválidas.
 * Mapea a HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
