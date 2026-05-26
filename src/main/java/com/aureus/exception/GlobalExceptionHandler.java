package com.aureus.exception;

import com.aureus.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones (U11 §5 — @ControllerAdvice).
 *
 * CAMBIOS vs versión anterior:
 *  - Movido de 'controller/' a 'controller/advice/' (separación lógica).
 *  - Usa la jerarquía de excepciones de dominio individuales (U11 §5.4).
 *  - Logging estructurado: WARN para 4xx (cliente), ERROR para 5xx (servidor).
 *  - Nunca expone stack traces al usuario (A05 OWASP - Security Misconfiguration).
 *  - Captura AureusException como base de negocio (abierto a extensión — OCP).
 *  - CORREGIDO: NoResourceFoundException (favicon.ico, etc.) maneja 404 silencioso.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja recursos estáticos inexistentes (favicon.ico, etc.).
     * Devuelve 404 sin log ni vista de error para evitar falsos positivos.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) {
        // Silencioso — no loguear como error: es una petición 404 normal
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccesoDenegado(AccessDeniedException ex, Model model) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return errorView(model, 403, "Acceso denegado",
                "No tienes permiso para acceder a este recurso.");
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccesoDenegadoNegocio(AccesoDenegadoException ex, Model model) {
        log.warn("IDOR attempt bloqueado: {}", ex.getMessage());
        return errorView(model, 403, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoEncontrado(RecursoNoEncontradoException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return errorView(model, 404, "Recurso no encontrado",
                "El recurso solicitado no existe.");
    }

    @ExceptionHandler({ValidacionException.class, EmailDuplicadoException.class,
                       PasswordIncorrectoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidacion(AureusException ex, Model model) {
        log.warn("Error de validación: {}", ex.getMessage());
        return errorView(model, 400, "Datos inválidos", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        // ERROR (no WARN) — requiere atención del equipo (U11 §6.1)
        log.error("Error inesperado en la aplicación", ex);
        return errorView(model, 500, "Error del servidor",
                "Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.");
    }

    // ── Helper: reduce duplicación en la construcción del modelo de error ──

    private String errorView(Model model, int codigo, String titulo, String mensaje) {
        model.addAttribute("codigo", codigo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensaje", mensaje);
        return "error/error";
    }
}
