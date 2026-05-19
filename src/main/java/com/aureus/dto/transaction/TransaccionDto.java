package com.aureus.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TransaccionDto {

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo; // "GASTO" o "INGRESO"

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private double amount;

    @NotBlank(message = "La fecha es obligatoria")
    private String date;

    private String description;
    private Long categoryId;

    @NotNull(message = "La cuenta es obligatoria")
    private Long accountId;

    private boolean isRecurring;
    private String tipoGasto;  // solo para gastos: FIJO / VARIABLE
    private String source;     // solo para ingresos
}
