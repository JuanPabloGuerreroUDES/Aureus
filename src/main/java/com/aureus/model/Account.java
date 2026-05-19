package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Cuenta.
 *
 * CORRECCIÓN: los métodos que sumaban Transaction::getAmount ahora usan
 * getAmountAsDouble() porque amount es BigDecimal.
 */
@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cuenta_usuario"))
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Budget> budgets = new ArrayList<>();

    public Account(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Transaction> getTransactionsByMonth(int month, int year) {
        return transactions.stream()
                .filter(t -> t.getDate().getMonthValue() == month
                          && t.getDate().getYear() == year)
                .toList();
    }

    public double getExpensesByCategory(Category category) {
        return transactions.stream()
                .filter(t -> t instanceof Expense)
                .filter(t -> category.equals(t.getCategory()))
                .mapToDouble(Transaction::getAmountAsDouble)   // ← corregido
                .sum();
    }

    public double getMonthlyExpenses(int month, int year) {
        return getTransactionsByMonth(month, year).stream()
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getAmountAsDouble)   // ← corregido
                .sum();
    }

    public double getBalance() {
        double ingresos = transactions.stream()
                .filter(t -> t instanceof Income)
                .mapToDouble(Transaction::getAmountAsDouble)   // ← corregido
                .sum();
        double gastos = transactions.stream()
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getAmountAsDouble)   // ← corregido
                .sum();
        return ingresos - gastos;
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", name='" + name + "'}";
    }
}
