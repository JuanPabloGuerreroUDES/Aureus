package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad abstracta Transacción — base de la jerarquía de herencia.
 *
 * CORRECCIÓN Hibernate 6:
 *   @Column(precision, scale) solo es válido para tipos SQL NUMERIC/DECIMAL,
 *   que en Java se mapea con BigDecimal. Usando 'double' Hibernate lanza:
 *   "scale has no meaning for SQL floating point types".
 *   Se cambió 'amount' de double → BigDecimal.
 */
@Entity
@Table(name = "transacciones")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 10)
@Getter
@Setter
@NoArgsConstructor
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "es_recurrente", nullable = false)
    private boolean isRecurring = false;

    /**
     * Monto de la transacción.
     * BigDecimal → SQL DECIMAL(15,2) — permite precision y scale en Hibernate 6.
     */
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate date;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transaccion_cuenta"))
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id",
                foreignKey = @ForeignKey(name = "fk_transaccion_categoria"))
    private Category category;

    // ── Constructor de conveniencia (acepta double y convierte) ──────────

    public Transaction(double amount, LocalDate date, String description,
                       Account account, Category category) {
        this.amount      = BigDecimal.valueOf(amount);
        this.date        = date;
        this.description = description;
        this.account     = account;
        this.category    = category;
    }

    /** Constructor alternativo con BigDecimal directamente. */
    public Transaction(BigDecimal amount, LocalDate date, String description,
                       Account account, Category category) {
        this.amount      = amount;
        this.date        = date;
        this.description = description;
        this.account     = account;
        this.category    = category;
    }

    // ── Helper: double para cálculos en memoria ──────────────────────────

    public double getAmountAsDouble() {
        return amount != null ? amount.doubleValue() : 0.0;
    }

    // Lombok @Setter genera setAmount(BigDecimal); añadimos el de double:
    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public abstract String getType();

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{id=" + id + ", amount=" + amount + ", date=" + date + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction t)) return false;
        return id != null && id.equals(t.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
