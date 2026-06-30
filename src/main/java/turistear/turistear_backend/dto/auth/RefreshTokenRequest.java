package turistear.turistear_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
