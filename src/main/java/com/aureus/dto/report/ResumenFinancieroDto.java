package com.aureus.dto.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de solo lectura para el dashboard y reportes.
 *
 * Distingue dos conceptos de balance:
 *
 *   balanceNeto   → ingresos - gastos DEL PERÍODO (mes seleccionado).
 *                   Refleja la variación financiera en ese intervalo.
 *                   Útil para reportes mensuales y tendencias.
 *
 *   balanceTotal  → SUMA ACUMULADA de todos los ingresos - todos los gastos
 *                   desde el inicio de la cuenta.
 *                   Es el "saldo real" del usuario: lo que realmente tiene.
 *                   Se muestra como KPI principal en el Dashboard.
 */
@Getter @Setter @NoArgsConstructor
public class ResumenFinancieroDto {

    // ── KPIs del período (mes) ────────────────────────────────────────────
    private double totalIngresos;   // ingresos del mes
    private double totalGastos;     // gastos del mes
    private double balanceNeto;     // ingresos - gastos del mes
    private double tasaAhorro;      // (balanceNeto / totalIngresos) * 100

    // ── Balance acumulado histórico (toda la cuenta) ──────────────────────
    private double balanceTotal;    // suma de TODAS las tx desde el inicio
    private double totalIngresosHistorico;
    private double totalGastosHistorico;

    private String periodoLabel;    // "Mayo 2026"
}
