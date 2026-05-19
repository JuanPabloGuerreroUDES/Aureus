package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad Presupuesto.
 *
 * REFACTORIZACIÓN (U11 - Clean Code / DRY):
 *   Se eliminó el campo @Transient 'gastoActual' que almacenaba estado mutable
 *   en una entidad JPA. Esto causaba:
 *     1. Acoplamiento temporal: checkAlert() dependía de que setGastoActual()
 *        hubiera sido llamado antes (implícito, no expresado en la firma).
 *     2. Diseño frágil: dos operaciones separadas para verificar una sola cosa.
 *   Solución: los métodos de negocio ahora reciben 'gastoCalculado' como parámetro
 *   explícito, haciendo la dependencia visible y el contrato claro (U11 §3.2).
 */
@Entity
@Table(name = "presupuestos")
@Getter @Setter @NoArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.01", message = "El límite debe ser mayor a cero")
    @Column(name = "monto_limite", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate endDate;

    @Column(name = "umbral_alerta", nullable = false)
    private int umbralAlertaPorcentaje = 80;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_presupuesto_cuenta"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_presupuesto_categoria"))
    private Category category;

    public Budget(double limitAmount, LocalDate startDate, LocalDate endDate,
                  Account account, Category category) {
        this.limitAmount = BigDecimal.valueOf(limitAmount);
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.account     = account;
        this.category    = category;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = BigDecimal.valueOf(limitAmount);
    }

    // ── Métodos de dominio (reciben el gasto como parámetro explícito) ────

    /**
     * Verifica si el gasto supera el umbral de alerta.
     * El llamador calcula el gasto total y lo pasa aquí — sin estado implícito.
     *
     * @param gastoCalculado total de gastos del período calculado por el servicio
     * @return true si el gasto supera el umbral configurado
     */
    public boolean checkAlert(double gastoCalculado) {
        if (limitAmount == null || limitAmount.doubleValue() <= 0) return false;
        double porcentaje = (gastoCalculado / limitAmount.doubleValue()) * 100.0;
        return porcentaje >= umbralAlertaPorcentaje;
    }

    public double calcularPorcentajeUso(double gastoCalculado) {
        if (limitAmount == null || limitAmount.doubleValue() <= 0) return 0;
        return (gastoCalculado / limitAmount.doubleValue()) * 100.0;
    }

    public double calcularRestante(double gastoCalculado) {
        return limitAmount != null ? limitAmount.doubleValue() - gastoCalculado : 0;
    }

    @Override
    public String toString() {
        return "Budget{id=" + id + ", category="
               + (category != null ? category.getName() : "null")
               + ", limit=" + limitAmount + "}";
    }
}
