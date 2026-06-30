package turistear.turistear_backend.exception;

/**
 * Indica que el usuario está autenticado pero no tiene permisos para
 * realizar la acción solicitada.
 * Mapea a HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
