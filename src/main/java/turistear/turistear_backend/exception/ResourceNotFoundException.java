package turistear.turistear_backend.exception;

/**
 * Indica que un recurso solicitado no existe en la base de datos.
 * Mapea a HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " no encontrado con id: " + id);
    }
}
