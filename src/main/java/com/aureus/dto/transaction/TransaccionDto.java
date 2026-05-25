package com.aureus.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para crear o editar una transacción.
 * Documentado con @Schema (U11 §7.4).
 */
@Getter @Setter @NoArgsConstructor
@Schema(description = "Datos para registrar una transacción (gasto o ingreso)")
public class TransaccionDto {

    @Schema(
        description      = "Tipo de transacción",
        example          = "GASTO",
        allowableValues  = {"GASTO", "INGRESO"},
        requiredMode     = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @Schema(description = "Monto de la transacción en pesos", example = "45000.00")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private double amount;

    @Schema(description = "Fecha de la transacción (formato ISO-8601)", example = "2026-05-25")
    @NotBlank(message = "La fecha es obligatoria")
    private String date;

    @Schema(description = "Descripción opcional de la transacción", example = "Compras en Éxito")
    private String description;

    @Schema(description = "ID de la categoría asociada (opcional)", example = "3")
    private Long categoryId;

    @Schema(description = "ID de la cuenta financiera del usuario", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La cuenta es obligatoria")
    private Long accountId;

    @Schema(description = "Indica si es una transacción recurrente (mensual, etc.)",
            example = "false")
    private boolean isRecurring;

    @Schema(
        description     = "Tipo de gasto — solo aplica para tipo GASTO",
        example         = "VARIABLE",
        allowableValues = {"FIJO", "VARIABLE"}
    )
    private String tipoGasto;

    @Schema(description = "Fuente del ingreso — solo aplica para tipo INGRESO",
            example     = "Salario")
    private String source;
}
