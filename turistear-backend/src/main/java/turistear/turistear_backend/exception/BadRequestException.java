package turistear.turistear_backend.exception;

/**
 * Indica que la request es inválida a nivel de reglas de negocio
 * (datos correctos en formato, pero no aceptables en el contexto).
 * Mapea a HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
