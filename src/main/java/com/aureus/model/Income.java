package com.aureus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad Ingreso — subclase de Transaction.
 *
 * Diagrama de clases: Income
 *   - source: String
 *   - transaction: Transaction  ← modelado como herencia
 *   + assignCategory(category: Category): void
 *
 * Fuentes típicas: "Salario", "Freelance", "Arriendo", "Inversión", "Otros"
 */
@Entity
@Table(name = "ingresos")
@PrimaryKeyJoinColumn(name = "transaccion_id",
                      foreignKey = @ForeignKey(name = "fk_ingreso_transaccion"))
@DiscriminatorValue("INGRESO")
@Getter
@Setter
@NoArgsConstructor
public class Income extends Transaction {

    // ── Atributos del diagrama ────────────────────────────────────────────

    /**
     * Fuente del ingreso: "Salario", "Freelance", "Inversión", etc.
     */
    @Column(name = "fuente", length = 100)
    private String source;

    // ── Constructor de conveniencia ───────────────────────────────────────

    public Income(double amount, LocalDate date, String description,
                  Account account, Category category, String source) {
        super(amount, date, description, account, category);
        this.source = source;
    }

    // ── Métodos del diagrama ──────────────────────────────────────────────

    /**
     * Asigna o cambia la categoría del ingreso.
     *
     * @param category nueva categoría
     */
    public void assignCategory(Category category) {
        setCategory(category);
    }

    /**
     * Tipo de transacción para vistas y reportes.
     */
    @Override
    public String getType() {
        return "INGRESO";
    }
}
