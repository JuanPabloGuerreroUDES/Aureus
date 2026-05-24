package com.aureus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad JWT — sección 7 de la Guía U9.
 *
 * Responsabilidades:
 *   - Generar tokens firmados con HS256 (HMAC-SHA256).
 *   - Validar la firma y la expiración.
 *   - Extraer claims (email, rol) del payload.
 *
 * Advertencia (U9 §7.1): el payload está codificado en Base64, NO cifrado.
 * Nunca incluir contraseñas ni datos sensibles en los claims.
 *
 * Estructura del token: header.payload.signature
 *   header  → {"alg":"HS256","typ":"JWT"}
 *   payload → {"sub":"email","rol":"ROLE_USER","iat":...,"exp":...}
 *   firma   → HMACSHA256(base64(header)+"."+base64(payload), secretKey)
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // ── Clave HMAC derivada del secreto configurado ───────────────────────

    private SecretKey getSigningKey() {
        // La clave debe tener al menos 256 bits para HS256
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ── Generación ────────────────────────────────────────────────────────

    /**
     * Genera un JWT firmado para el usuario autenticado.
     *
     * @param email correo del usuario (claim "sub")
     * @param rol   rol del usuario — "ROLE_USER" o "ROLE_ADMIN" (claim personalizado)
     * @return token JWT compacto (header.payload.signature)
     */
    public String generateToken(String email, String rol) {
        Date ahora      = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)                     // claim "sub" = identificador del usuario
                .claim("rol", rol)                  // claim personalizado: rol de Spring Security
                .issuedAt(ahora)                    // claim "iat": timestamp de emisión
                .expiration(expiracion)             // claim "exp": timestamp de vencimiento
                .signWith(getSigningKey())          // firma HMAC-SHA256
                .compact();
    }

    // ── Validación ────────────────────────────────────────────────────────

    /**
     * Verifica que el token tenga firma válida y no haya expirado.
     * No lanza excepción — devuelve false si cualquier validación falla (Fail Secure, U9 §1.2).
     *
     * @param token JWT a validar
     * @return true si el token es válido y vigente
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);  // lanza JwtException si es inválido o expirado
            return true;
        } catch (JwtException e) {
            log.warn("JWT rechazado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vacío o nulo: {}", e.getMessage());
        }
        return false;
    }

    // ── Extracción de claims ──────────────────────────────────────────────

    /**
     * Extrae el email (claim "sub") del token.
     * Solo llamar después de validateToken() == true.
     *
     * @param token JWT válido
     * @return email del usuario
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     *
     * @param token JWT válido
     * @return rol del usuario (ej. "ROLE_USER")
     */
    public String getRolFromToken(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())     // verifica firma
                .build()
                .parseSignedClaims(token)       // lanza ExpiredJwtException si expiró
                .getPayload();
    }
}
