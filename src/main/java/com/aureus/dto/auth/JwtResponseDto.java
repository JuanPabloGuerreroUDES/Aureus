package com.aureus.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de respuesta del endpoint POST /api/auth/login.
 *
 * Contiene el token JWT que el cliente debe incluir en peticiones
 * subsiguientes como: Authorization: Bearer <token>
 *
 * Advertencia (U9 §7.1): el payload del JWT NO está cifrado.
 * Este DTO tampoco debe exponer datos sensibles.
 */
@Getter
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String email;
    private String rol;
    private String tipo = "Bearer";

    public JwtResponseDto(String token, String email, String rol) {
        this.token = token;
        this.email = email;
        this.rol   = rol;
    }
}
