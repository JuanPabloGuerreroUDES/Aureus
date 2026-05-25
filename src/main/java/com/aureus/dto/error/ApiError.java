package com.aureus.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de error estandarizada para la capa REST (U11 §5.2).
 *
 * Soluciona los problemas del manejo de errores sin centralizar (U11 §5.1):
 *   ✅ Formato JSON uniforme en todos los errores de /api/**
 *   ✅ Código HTTP explícito y correcto (404, 400, 403, 500)
 *   ✅ Timestamp para correlación en logs y monitoreo
 *   ✅ Path del request para identificar qué endpoint falló
 *   ✅ Sin stack traces expuestos al cliente (A05 OWASP)
 *
 * Diseñada como POJO inmutable (los campos se asignan en el constructor)
 * para evitar modificaciones accidentales del estado de error.
 *
 * Ejemplo de respuesta JSON:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "mensaje": "Transacción con id 99 no encontrada",
 *   "timestamp": "2026-05-25T10:30:00",
 *   "path": "/api/transacciones/99"
 * }
 */
@Getter
@Schema(description = "Estructura de respuesta de error para endpoints REST")
public class ApiError {

    @Schema(description = "Código de estado HTTP", example = "404")
    private final int status;

    @Schema(description = "Categoría del error", example = "Not Found")
    private final String error;

    @Schema(description = "Descripción legible del error", example = "Recurso con id 5 no encontrado")
    private final String mensaje;

    @Schema(description = "Momento exacto del error (ISO-8601)", example = "2026-05-25T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(description = "Ruta del endpoint que generó el error", example = "/api/auth/login")
    private final String path;

    // ── Constructor principal ─────────────────────────────────────────────

    /**
     * Constructor completo. El timestamp se asigna automáticamente al momento
     * de la construcción para garantizar precisión.
     *
     * @param status  código HTTP (ej. 404, 400, 500)
     * @param error   categoría legible (ej. "Not Found", "Bad Request")
     * @param mensaje descripción específica del problema
     * @param path    URI del request que generó el error
     */
    public ApiError(int status, String error, String mensaje, String path) {
        this.status    = status;
        this.error     = error;
        this.mensaje   = mensaje;
        this.timestamp = LocalDateTime.now();
        this.path      = path;
    }

    // ── Factory methods (DRY — evitan repetir status + error string) ──────

    /** Crea un ApiError 400 Bad Request */
    public static ApiError badRequest(String mensaje, String path) {
        return new ApiError(400, "Bad Request", mensaje, path);
    }

    /** Crea un ApiError 401 Unauthorized */
    public static ApiError unauthorized(String mensaje, String path) {
        return new ApiError(401, "Unauthorized", mensaje, path);
    }

    /** Crea un ApiError 403 Forbidden */
    public static ApiError forbidden(String mensaje, String path) {
        return new ApiError(403, "Forbidden", mensaje, path);
    }

    /** Crea un ApiError 404 Not Found */
    public static ApiError notFound(String mensaje, String path) {
        return new ApiError(404, "Not Found", mensaje, path);
    }

    /** Crea un ApiError 409 Conflict */
    public static ApiError conflict(String mensaje, String path) {
        return new ApiError(409, "Conflict", mensaje, path);
    }

    /** Crea un ApiError 500 Internal Server Error con mensaje genérico (seguridad) */
    public static ApiError internalError(String path) {
        return new ApiError(500, "Internal Server Error",
                "Error inesperado. Contactar al equipo de soporte.", path);
    }
}
