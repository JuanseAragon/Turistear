package turistear.turistear_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de OpenAPI / Swagger.
 *
 * - Declara el esquema de seguridad "bearerAuth" (JWT en header Authorization).
 * - Lo aplica como requerimiento global, así TODOS los endpoints aparecen como
 *   protegidos en la UI por defecto. Los que no requieren token (register / login)
 *   se opt-out con @SecurityRequirements({}) en el controller.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TuristeAR API",
                version = "1.0.0",
                description = "API REST de TuristeAR — La mejor aplicacion " +
                        "para crear y compartir itinerarios de viaje por Argentina. " 
        ),
        security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Pegá únicamente el token (sin el prefijo 'Bearer '). Swagger se encarga del resto."
)
public class OpenApiConfig {
}
