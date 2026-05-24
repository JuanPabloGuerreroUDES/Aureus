package com.aureus.service;

import com.aureus.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias de JwtUtils (U9 §7 — JWT Stateless).
 *
 * No necesita Spring context — JwtUtils es un @Component sin dependencias
 * externas más allá de los valores @Value inyectados con ReflectionTestUtils.
 *
 * Nomenclatura: metodo_condicion_resultadoEsperado
 * Estructura: Given-When-Then
 */
@DisplayName("JwtUtils")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    /** Secreto de 64+ chars para cumplir el mínimo de 256 bits para HMAC-SHA256 */
    private static final String SECRET =
            "TestSecretKeyParaJWTDebeSerLargaYSegura!!XYZ123AbcDef456GhiJkl789";
    private static final long   EXPIRATION_MS = 86_400_000L;  // 24 horas

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",      SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    // ── Generación y extracción ───────────────────────────────────────────

    @Nested
    @DisplayName("Al generar un token")
    class Generacion {

        @Test
        @DisplayName("el token contiene el email en el claim 'sub'")
        void generateToken_conEmailYRol_subjectEsEmail() {
            // When
            String token = jwtUtils.generateToken("user@test.com", "ROLE_USER");

            // Then
            assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("el token contiene el rol en el claim personalizado")
        void generateToken_conEmailYRol_claimRolCorrecto() {
            // When
            String token = jwtUtils.generateToken("admin@test.com", "ROLE_ADMIN");

            // Then
            assertThat(jwtUtils.getRolFromToken(token)).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("el mismo email produce tokens distintos (unicidad por timestamp)")
        void generateToken_mismosParametros_tokensDistintos() throws InterruptedException {
            // Given — pequeña pausa para garantizar diferencia en 'iat'
            String token1 = jwtUtils.generateToken("user@test.com", "ROLE_USER");
            Thread.sleep(10);
            String token2 = jwtUtils.generateToken("user@test.com", "ROLE_USER");

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // ── Validación ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Al validar un token")
    class Validacion {

        @Test
        @DisplayName("un token recién generado es válido")
        void validateToken_tokenReciente_retornaTrue() {
            // Given
            String token = jwtUtils.generateToken("user@test.com", "ROLE_USER");

            // When / Then
            assertThat(jwtUtils.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("un token expirado es rechazado (Fail Secure — U9 §1.2)")
        void validateToken_tokenExpirado_retornaFalse() {
            // Given — expiración en el pasado (−1 ms)
            ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1L);
            String tokenExpirado = jwtUtils.generateToken("user@test.com", "ROLE_USER");

            // Restaurar para que no afecte otros tests
            ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);

            // When / Then
            assertThat(jwtUtils.validateToken(tokenExpirado)).isFalse();
        }

        @Test
        @DisplayName("un token manipulado (firma inválida) es rechazado")
        void validateToken_tokenManipulado_retornaFalse() {
            // Given — token válido con firma alterada
            String token = jwtUtils.generateToken("user@test.com", "ROLE_USER");
            String tokenManipulado = token.substring(0, token.length() - 5) + "XXXXX";

            // When / Then
            assertThat(jwtUtils.validateToken(tokenManipulado)).isFalse();
        }

        @Test
        @DisplayName("null o string vacío no lanza excepción — devuelve false")
        void validateToken_nulo_retornaFalseSinExcepcion() {
            assertThat(jwtUtils.validateToken(null)).isFalse();
            assertThat(jwtUtils.validateToken("")).isFalse();
        }
    }
}
