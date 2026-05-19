package com.aureus.dto.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO de solo lectura para el dashboard y reportes (no mapea a entidad). */
@Getter @Setter @NoArgsConstructor
public class ResumenFinancieroDto {
    private double totalIngresos;
    private double totalGastos;
    private double balanceNeto;
    private double tasaAhorro;
    private String periodoLabel;
}
