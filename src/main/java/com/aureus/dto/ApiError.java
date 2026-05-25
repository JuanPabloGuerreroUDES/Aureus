package com.aureus.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de error estandarizada para endpoints REST (U11 §5.2).
 *
 * Motivación (U11 §5.1): sin este DTO, cada controlador genera errores
 * en formatos distintos (texto plano, HTML de Spring, JSON parcial).
 * ApiError garantiza un contrato uniforme para todos los errores de /api/**.
 *
 * El patrón DTO (U11 §4.3) se aplica aquí: esta clase solo transporta
 * datos de error hacia el cliente, sin lógica de negocio.
 *
 * Decisión de diseño — @Schema (U11 §7.4):
 *   Documenta cada campo con descripción y ejemplo para que Swagger UI
 *   muestre respuestas de error reales en la documentación interactiva.
 *
 * Nunca incluir:
 *   - Stack traces (expondrían detalles internos — A05 OWASP)
 *   - Nombres de clases internas
 *   - Mensajes de la base de datos
 */
@Getter
@Schema(description = "Respuesta de error estandarizada de la API REST de Aureus")
public class ApiError {

    @Schema(description = "Código de estado HTTP", example = "404")
    private final int status;

    @Schema(description = "Categoría del error HTTP", example = "Not Found")
    private final String error;

    @Schema(
        description = "Mensaje de error legible para el cliente",
        example     = "Transacción con id 99 no encontrada"
    )
    private final String mensaje;

    @Schema(
        description = "Fecha y hora del error en formato ISO-8601",
        example     = "2026-05-24T15:30:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(
        description = "Ruta del endpoint que generó el error",
        example     = "/api/transacciones/99"
    )
    private final String path;

    // ── Constructor principal ─────────────────────────────────────────────

    /**
     * Crea un ApiError con timestamp automático del momento del error.
     *
     * @param status  código HTTP (400, 401, 403, 404, 409, 500)
     * @param error   categoría del error ("Not Found", "Bad Request", etc.)
     * @param mensaje mensaje legible — sin detalles internos (A05 OWASP)
     * @param path    URI de la petición que causó el error
     */
    public ApiError(int status, String error, String mensaje, String path) {
        this.status    = status;
        this.error     = error;
        this.mensaje   = mensaje;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }
}
