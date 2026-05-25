package com.aureus.controller.advice;

import com.aureus.dto.error.ApiError;
import com.aureus.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador centralizado de excepciones para la capa REST /api/** (U11 §5.3).
 *
 * Implementa el diseño de la guía:
 *   §5.1 — Un único punto de manejo por tipo de excepción (elimina try/catch duplicados)
 *   §5.2 — Retorna ApiError estandarizado en formato JSON
 *   §5.3 — @RestControllerAdvice: intercepta y transforma en ResponseEntity
 *   §5.4 — Usa la jerarquía de excepciones de negocio (AureusException)
 *
 * ARQUITECTURA DUAL (coexiste con GlobalExceptionHandler):
 *   • GlobalExceptionHandler (@ControllerAdvice)  → capa MVC, retorna vistas JSP
 *   • ApiExceptionHandler   (@RestControllerAdvice, assignableTypes = REST controllers)
 *                                                 → capa REST, retorna JSON ApiError
 *
 * Principio SOLID — OCP (§2.2):
 *   Agregar nuevos tipos de excepción de negocio solo requiere añadir un nuevo
 *   @ExceptionHandler, sin modificar los existentes.
 *
 * Logging (U11 §6):
 *   WARN para errores 4xx (cliente — situación esperada, recuperable).
 *   ERROR para errores 5xx (servidor — requiere atención del equipo).
 *   Placeholders {} en lugar de concatenación de strings (U11 §6.4).
 *   Nunca registra passwords, tokens ni datos personales completos.
 */
@RestControllerAdvice(basePackages = "com.aureus.controller")
@Slf4j
public class ApiExceptionHandler {

    // ── 401 Unauthorized — credenciales inválidas ─────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentials(BadCredentialsException ex,
                                         HttpServletRequest req) {
        // WARN (no ERROR): credenciales incorrectas son un evento esperado
        log.warn("Credenciales inválidas en: {}", req.getRequestURI());
        // Mensaje genérico: no revelar si el email existe (A07 OWASP)
        return ApiError.unauthorized("Credenciales inválidas o cuenta inactiva", req.getRequestURI());
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleDisabled(DisabledException ex, HttpServletRequest req) {
        log.warn("Intento de login con cuenta desactivada en: {}", req.getRequestURI());
        return ApiError.unauthorized("Credenciales inválidas o cuenta inactiva", req.getRequestURI());
    }

    // ── 403 Forbidden — acceso denegado (IDOR y roles) ───────────────────

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Acceso denegado a {} — {}", req.getRequestURI(), ex.getMessage());
        return ApiError.forbidden("No tienes permiso para acceder a este recurso.", req.getRequestURI());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccesoDenegadoNegocio(AccesoDenegadoException ex,
                                                 HttpServletRequest req) {
        log.warn("IDOR bloqueado en {}: {}", req.getRequestURI(), ex.getMessage());
        return ApiError.forbidden(ex.getMessage(), req.getRequestURI());
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoEncontrado(RecursoNoEncontradoException ex,
                                        HttpServletRequest req) {
        log.warn("Recurso no encontrado en {}: {}", req.getRequestURI(), ex.getMessage());
        return ApiError.notFound(ex.getMessage(), req.getRequestURI());
    }

    // ── 400 Bad Request — validación y negocio ────────────────────────────

    /**
     * Maneja @Valid + @RequestBody: agrega todos los errores de validación
     * de campos en un único mensaje concatenado (U11 §5.3).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex,
                                            HttpServletRequest req) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Error de validación en {}: {}", req.getRequestURI(), errores);
        return ApiError.badRequest(errores, req.getRequestURI());
    }

    @ExceptionHandler(ValidacionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidacion(ValidacionException ex, HttpServletRequest req) {
        log.warn("Validación de negocio en {}: {}", req.getRequestURI(), ex.getMessage());
        return ApiError.badRequest(ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PasswordIncorrectoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlePasswordIncorrecto(PasswordIncorrectoException ex,
                                              HttpServletRequest req) {
        log.warn("Contraseña incorrecta en: {}", req.getRequestURI());
        return ApiError.badRequest(ex.getMessage(), req.getRequestURI());
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────

    @ExceptionHandler(EmailDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailDuplicado(EmailDuplicadoException ex,
                                          HttpServletRequest req) {
        log.warn("Email duplicado en {}: {}", req.getRequestURI(), ex.getMessage());
        return ApiError.conflict(ex.getMessage(), req.getRequestURI());
    }

    // ── 415 Unsupported Media Type ────────────────────────────────────────

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiError handleMediaType(HttpMediaTypeNotSupportedException ex,
                                     HttpServletRequest req) {
        return new ApiError(415, "Unsupported Media Type",
                "Content-Type no soportado. Use application/json.", req.getRequestURI());
    }

    // ── 400 Bad Request — body JSON malformado ────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMalformedJson(HttpMessageNotReadableException ex,
                                         HttpServletRequest req) {
        log.warn("JSON malformado en {}", req.getRequestURI());
        return ApiError.badRequest("El cuerpo de la petición no tiene un formato JSON válido.",
                req.getRequestURI());
    }

    // ── 500 Internal Server Error — catch-all ────────────────────────────

    /**
     * Captura cualquier excepción no manejada explícitamente.
     *
     * ERROR (no WARN): situación inesperada que requiere atención del equipo.
     * El mensaje al cliente es genérico: nunca exponer stack traces (A05 OWASP).
     * El stack trace completo queda en los logs del servidor.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return ApiError.internalError(req.getRequestURI());
    }
}
