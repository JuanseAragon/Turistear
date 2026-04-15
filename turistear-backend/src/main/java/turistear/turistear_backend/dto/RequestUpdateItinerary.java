package turistear.turistear_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record RequestUpdateItinerary(
                @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
                @Size(max = 2000, message = "La descripcion no puede superar los 2000 caracteres")
        String descripcion,
        Boolean esPublico
) {

        @AssertTrue(message = "Debe enviar al menos un campo para actualizar")
        public boolean isAnyFieldPresent() {
                return nombre != null || descripcion != null || esPublico != null;
        }

        @AssertTrue(message = "El nombre no puede estar vacio")
        public boolean isNombreValid() {
                return nombre == null || !nombre.isBlank();
        }

        @AssertTrue(message = "La descripcion no puede estar vacia")
        public boolean isDescripcionValid() {
                return descripcion == null || !descripcion.isBlank();
        }
}
