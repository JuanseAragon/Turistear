package turistear.turistear_backend.exception;

/**
 * Indica que la request entra en conflicto con el estado actual del recurso
 * (por ejemplo, un email ya registrado o un usuario que ya fue eliminado).
 * Mapea a HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
