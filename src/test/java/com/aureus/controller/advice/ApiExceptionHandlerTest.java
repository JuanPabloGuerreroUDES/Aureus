package com.aureus.controller.advice;

import com.aureus.dto.error.ApiError;
import com.aureus.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de ApiExceptionHandler (U11 §5.3).
 *
 * Prueba que cada tipo de excepción se mapea al código HTTP correcto
 * y que el ApiError resultante tiene los campos esperados (U11 §5.2).
 *
 * No usa Spring context — instancia el handler directamente para velocidad
 * (prueba unitaria pura, posición en la base de la pirámide — U10 §1.3).
 */
@DisplayName("ApiExceptionHandler — mapeo excepción → ApiError")
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    private MockHttpServletRequest req(String uri) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRequestURI(uri);
        return r;
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Excepciones de autenticación")
    class Autenticacion {

        @Test
        @DisplayName("BadCredentialsException → 401 con mensaje genérico")
        void badCredentials_retorna401ConMensajeGenerico() {
            // Given
            BadCredentialsException ex = new BadCredentialsException("Bad credentials");

            // When
            ApiError error = handler.handleBadCredentials(ex, req("/api/auth/login"));

            // Then
            assertThat(error.getStatus()).isEqualTo(401);
            assertThat(error.getError()).isEqualTo("Unauthorized");
            // Verificar que el mensaje es genérico (A07 OWASP — no revelar si el email existe)
            assertThat(error.getMensaje()).doesNotContain("email", "usuario", "no existe");
            assertThat(error.getPath()).isEqualTo("/api/auth/login");
            assertThat(error.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("DisabledException → 401 con mismo mensaje genérico")
        void disabled_retorna401ConMensajeGenerico() {
            // When
            ApiError error = handler.handleDisabled(new DisabledException("disabled"), req("/api/auth/login"));

            // Then — mismo mensaje que BadCredentials (no revelar razón específica)
            assertThat(error.getStatus()).isEqualTo(401);
            assertThat(error.getMensaje()).isEqualTo("Credenciales inválidas o cuenta inactiva");
        }
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Excepciones de acceso denegado")
    class AccesoDenegado {

        @Test
        @DisplayName("AccesoDenegadoException → 403 (protección IDOR)")
        void accesoDenegado_retorna403() {
            // When
            ApiError error = handler.handleAccesoDenegadoNegocio(
                    new AccesoDenegadoException(), req("/api/presupuestos/99"));

            // Then
            assertThat(error.getStatus()).isEqualTo(403);
            assertThat(error.getError()).isEqualTo("Forbidden");
        }
    }

    // ── 404 Not Found ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Recurso no encontrado")
    class NoEncontrado {

        @Test
        @DisplayName("RecursoNoEncontradoException → 404 con mensaje del dominio")
        void recursoNoEncontrado_retorna404ConMensajeDominio() {
            // Given
            RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Transacción", 99L);

            // When
            ApiError error = handler.handleNoEncontrado(ex, req("/api/transacciones/99"));

            // Then
            assertThat(error.getStatus()).isEqualTo(404);
            assertThat(error.getMensaje()).contains("99");
        }
    }

    // ── 409 Conflict ───────────────────────────────────────────────────────

    @Test
    @DisplayName("EmailDuplicadoException → 409 Conflict")
    void emailDuplicado_retorna409() {
        // When
        ApiError error = handler.handleEmailDuplicado(
                new EmailDuplicadoException("test@test.com"),
                req("/api/auth/registro"));

        // Then
        assertThat(error.getStatus()).isEqualTo(409);
        assertThat(error.getError()).isEqualTo("Conflict");
    }

    // ── 500 Internal Server Error ─────────────────────────────────────────

    @Test
    @DisplayName("Exception inesperada → 500 con mensaje genérico (sin detalles internos)")
    void exceptionInesperada_retorna500ConMensajeGenerico() {
        // Given — NullPointerException inesperada
        NullPointerException ex = new NullPointerException("detalles internos");

        // When
        ApiError error = handler.handleGeneral(ex, req("/api/dashboard"));

        // Then — mensaje al cliente es genérico (A05 OWASP: no exponer stack traces)
        assertThat(error.getStatus()).isEqualTo(500);
        assertThat(error.getMensaje()).doesNotContain("detalles internos", "NullPointer");
        assertThat(error.getMensaje()).containsIgnoringCase("soporte");
    }

    // ── Estructura ApiError ───────────────────────────────────────────────

    @Nested
    @DisplayName("Estructura de ApiError (U11 §5.2)")
    class EstructuraApiError {

        @ParameterizedTest(name = "path={0} se almacena correctamente")
        @ValueSource(strings = {"/api/auth/login", "/api/transacciones/1", "/api/metas"})
        @DisplayName("el campo 'path' refleja la URI del request")
        void apiError_pathReflecteUriDelRequest(String uri) {
            // When
            ApiError error = handler.handleBadCredentials(
                    new BadCredentialsException("x"), req(uri));

            // Then
            assertThat(error.getPath()).isEqualTo(uri);
        }

        @Test
        @DisplayName("factory method ApiError.badRequest tiene status=400 y error='Bad Request'")
        void factoryMethod_badRequest_tieneStatus400() {
            ApiError error = ApiError.badRequest("campo: obligatorio", "/api/test");

            assertThat(error.getStatus()).isEqualTo(400);
            assertThat(error.getError()).isEqualTo("Bad Request");
            assertThat(error.getMensaje()).isEqualTo("campo: obligatorio");
            assertThat(error.getTimestamp()).isNotNull();
        }
    }
}
