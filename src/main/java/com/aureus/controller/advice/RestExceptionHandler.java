package com.aureus.controller.advice;

import com.aureus.dto.ApiError;
import com.aureus.exception.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador de excepciones para la capa REST (/api/**) — U11 §5.3.
 *
 * SEPARACIÓN DE RESPONSABILIDADES (U11 §2.1 — SRP):
 *   - GlobalExceptionHandler → controladores MVC → retorna vistas JSP
 *   - RestExceptionHandler   → controladores REST → retorna JSON ApiError
 *
 * @RestControllerAdvice(basePackages = "...controller") limita este handler
 * a los controladores dentro de ese paquete, mientras que la anotación
 * produce JSON automáticamente (= @ControllerAdvice + @ResponseBody).
 *
 * Principio OCP (U11 §2.2):
 *   Para manejar un nuevo tipo de excepción, se agrega un método @ExceptionHandler
 *   sin modificar los handlers existentes.
 *
 * Reglas de logging (U11 §6.1 + §6.4):
 *   - 4xx (error del cliente)  → log.warn  (situación inusual pero no crítica)
 *   - 5xx (error del servidor) → log.error (requiere atención del equipo)
 *   - Nunca loguear datos sensibles (contraseñas, tokens, datos personales)
 *   - Siempre usar placeholders {} en lugar de concatenación
 */
@RestControllerAdvice(basePackages = "com.aureus.controller")
@Slf4j
public class RestExceptionHandler {

    // ── 400 Bad Request ───────────────────────────────────────────────────

    /**
     * Maneja errores de validación de Bean Validation (@Valid en DTOs).
     * Consolida todos los errores de campo en un mensaje único.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidacionBean(MethodArgumentNotValidException ex,
                                         HttpServletRequest req) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validación fallida en {}: {}", req.getRequestURI(), errores);
        return new ApiError(400, "Bad Request", errores, req.getRequestURI());
    }

    /**
     * Maneja excepciones de validación de negocio (ej: contraseñas no coinciden).
     */
    @ExceptionHandler({ValidacionException.class, PasswordIncorrectoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidacionNegocio(AureusException ex, HttpServletRequest req) {
        log.warn("Error de validación de negocio en {}: {}", req.getRequestURI(), ex.getMessage());
        return new ApiError(400, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────────

    /**
     * Maneja intentos de acceso sin autenticación válida.
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleNoAutenticado(
            org.springframework.security.core.AuthenticationException ex,
            HttpServletRequest req) {
        // Mensaje genérico — no revelar si el recurso existe (OWASP A07)
        log.warn("Intento de acceso no autenticado a {}", req.getRequestURI());
        return new ApiError(401, "Unauthorized",
                "Autenticación requerida. Use POST /api/auth/login para obtener un token.",
                req.getRequestURI());
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────

    /**
     * Maneja intentos IDOR y acceso denegado por rol insuficiente (U9 §5).
     */
    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(Exception ex, HttpServletRequest req) {
        log.warn("Acceso denegado en {}: {}", req.getRequestURI(), ex.getMessage());
        return new ApiError(403, "Forbidden",
                "No tienes permisos para acceder a este recurso.",
                req.getRequestURI());
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────

    /**
     * Recurso solicitado no encontrado.
     * El mensaje genérico previene revelar si el recurso existe (IDOR — OWASP A01).
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoEncontrado(RecursoNoEncontradoException ex,
                                        HttpServletRequest req) {
        log.warn("Recurso no encontrado en {}: {}", req.getRequestURI(), ex.getMessage());
        return new ApiError(404, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────

    /**
     * Conflicto de datos (ej: email duplicado en registro).
     */
    @ExceptionHandler(EmailDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflicto(EmailDuplicadoException ex, HttpServletRequest req) {
        log.warn("Conflicto en {}: {}", req.getRequestURI(), ex.getMessage());
        return new ApiError(409, "Conflict", ex.getMessage(), req.getRequestURI());
    }

    // ── 500 Internal Server Error ─────────────────────────────────────────

    /**
     * Catch-all para errores inesperados del servidor.
     *
     * NUNCA exponer el mensaje de la excepción al cliente:
     *   - Puede contener SQL, nombres de clases, rutas internas (A05 OWASP).
     * El mensaje real va al log (visible para el equipo de soporte).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception ex, HttpServletRequest req) {
        // ERROR — nivel máximo para errores 5xx (U11 §6.1)
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return new ApiError(500, "Internal Server Error",
                "Ha ocurrido un error inesperado. Contacta soporte si persiste.",
                req.getRequestURI());
    }
}
