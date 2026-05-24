package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad MetaAhorro.
 *
 * CORRECCIÓN Hibernate 6: targetAmount y currentAmount cambiados
 * de double → BigDecimal para que @Column(precision=15, scale=2) sea válido.
 */
@Entity
@Table(name = "metas_ahorro")
@Getter
@Setter
@NoArgsConstructor
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Column(name = "nombre_meta", nullable = false, length = 150)
    private String goalName;

    @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor a cero")
    @Column(name = "monto_objetivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "monto_actual", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @NotNull(message = "La fecha límite es obligatoria")
    @Future(message = "La fecha límite debe ser futura")
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate deadline; // LocalDate → DATE en MySQL

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_meta_usuario"))
    private User user;

    // ── Constructor de conveniencia ───────────────────────────────────────

    public SavingsGoal(String goalName, double targetAmount, LocalDate deadline, User user) {
        this.goalName     = goalName;
        this.targetAmount = BigDecimal.valueOf(targetAmount);
        this.deadline     = deadline;
        this.user         = user;
    }

    // ── Setters de conveniencia para double ───────────────────────────────

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = BigDecimal.valueOf(targetAmount);
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = BigDecimal.valueOf(currentAmount);
    }

    // ── Métodos del diagrama ──────────────────────────────────────────────

    public double calculateProgress() {
        if (targetAmount == null || targetAmount.doubleValue() <= 0) return 0.0;
        double progreso = (currentAmount.doubleValue() / targetAmount.doubleValue()) * 100.0;
        return Math.min(progreso, 100.0);
    }

    public boolean checkCompletion() {
        if (targetAmount == null || currentAmount == null) return false;
        return currentAmount.compareTo(targetAmount) >= 0;
    }

    public void registrarAporte(double aporte) {
        if (aporte <= 0) {
            throw new IllegalArgumentException("El aporte debe ser mayor a cero");
        }
        this.currentAmount = this.currentAmount.add(BigDecimal.valueOf(aporte));
    }

    public double getMontoRestante() {
        if (targetAmount == null) return 0;
        double restante = targetAmount.doubleValue() - currentAmount.doubleValue();
        return Math.max(0, restante);
    }

    public boolean isVencida() {
        return LocalDate.now().isAfter(deadline) && !checkCompletion();
    }

    @Override
    public String toString() {
        return "SavingsGoal{id=" + id +
               ", name='" + goalName + "'" +
               ", progress=" + String.format("%.1f", calculateProgress()) + "%}";
    }
}
