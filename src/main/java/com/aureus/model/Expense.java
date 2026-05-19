package com.aureus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad Gasto — subclase de Transaction.
 *
 * Diagrama de clases: Expense
 *   - exceedsBudget: boolean
 *   - transaction: Transaction  ← modelado como herencia, no composición
 *   + assignCategory(category: Category): void
 *   + checkLimit(): boolean
 *
 * Notas de implementación:
 *   La relación "- transaction: Transaction" del diagrama se interpreta como
 *   herencia (Expense IS-A Transaction), no como composición, para mantener
 *   una sola tabla de transacciones y aprovechar el polimorfismo de JPA.
 */
@Entity
@Table(name = "gastos")
@PrimaryKeyJoinColumn(name = "transaccion_id",
                      foreignKey = @ForeignKey(name = "fk_gasto_transaccion"))
@DiscriminatorValue("GASTO")
@Getter
@Setter
@NoArgsConstructor
public class Expense extends Transaction {

    // ── Atributos del diagrama ────────────────────────────────────────────

    /**
     * Indica si este gasto superó el presupuesto de su categoría.
     * Se calcula al registrar el gasto comparándolo con el Budget vigente.
     */
    @Column(name = "supera_presupuesto", nullable = false)
    private boolean exceedsBudget = false;

    /**
     * Tipo de gasto: FIJO (alquiler, suscripciones) o VARIABLE (supermercado, ocio).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gasto", length = 10)
    private TipoGasto tipoGasto = TipoGasto.VARIABLE;

    // ── Constructor de conveniencia ───────────────────────────────────────

    public Expense(double amount, LocalDate date, String description,
                   Account account, Category category, TipoGasto tipoGasto) {
        super(amount, date, description, account, category);
        this.tipoGasto = tipoGasto;
    }

    // ── Métodos del diagrama ──────────────────────────────────────────────

    /**
     * Asigna o cambia la categoría del gasto.
     * Delega al setter heredado de Transaction.
     *
     * @param category nueva categoría
     */
    public void assignCategory(Category category) {
        setCategory(category);
    }

    /**
     * Verifica si este gasto supera el presupuesto asignado a su categoría.
     * La lógica real la ejecuta BudgetService; este método es del dominio.
     *
     * @return true si exceedsBudget está marcado
     */
    public boolean checkLimit() {
        return this.exceedsBudget;
    }

    /**
     * Tipo de transacción para vistas y reportes.
     */
    @Override
    public String getType() {
        return "GASTO";
    }

    // ── Enum interno ─────────────────────────────────────────────────────

    public enum TipoGasto {
        FIJO, VARIABLE
    }
}
