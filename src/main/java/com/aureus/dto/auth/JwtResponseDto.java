package com.aureus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de respuesta del endpoint POST /api/auth/login.
 * Documentado con @Schema para Swagger UI (U11 §7.4).
 *
 * Patrón DTO (U11 §4.3): expone solo los datos necesarios al cliente.
 * La contraseña hasheada y datos internos NUNCA se incluyen aquí.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Respuesta exitosa de autenticación JWT")
public class JwtResponseDto {

    @Schema(
        description = "Token JWT firmado con HS256. Incluir en requests como: Authorization: Bearer <token>",
        example     = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9.abc123"
    )
    private String token;

    @Schema(description = "Email del usuario autenticado", example = "juan.garcia@email.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "ROLE_USER",
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
    private String rol;

    @Schema(description = "Tipo de token — siempre Bearer", example = "Bearer")
    private String tipo = "Bearer";

    public JwtResponseDto(String token, String email, String rol) {
        this.token = token;
        this.email = email;
        this.rol   = rol;
    }
}
