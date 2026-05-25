package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.Category;
import com.aureus.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Transacción.
 *
 * Las consultas JPQL usan parámetros con nombre (:param) — nunca concatenación,
 * lo que garantiza protección contra SQL Injection.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ── Consultas con protección IDOR (filtran por cuenta/usuario) ────────

    /**
     * Todas las transacciones de una cuenta, ordenadas por fecha desc.
     * JOIN FETCH carga category y account en el mismo SELECT,
     * evitando LazyInitializationException al acceder desde JSP.
     */
    @Query("""
           SELECT t FROM Transaction t
           LEFT JOIN FETCH t.category
           JOIN FETCH t.account
           WHERE t.account = :account
           ORDER BY t.date DESC
           """)
    List<Transaction> findByAccountOrderByDateDesc(@Param("account") Account account);

    /** Busca transacción por ID y cuenta (protección IDOR). */
    Optional<Transaction> findByIdAndAccount(Long id, Account account);

    /**
     * Transacciones de una cuenta en un rango de fechas, con JOIN FETCH.
     * Usado para reportes mensuales — carga category en el mismo SELECT.
     */
    @Query("""
           SELECT t FROM Transaction t
           LEFT JOIN FETCH t.category
           JOIN FETCH t.account
           WHERE t.account = :account
             AND t.date BETWEEN :desde AND :hasta
           ORDER BY t.date DESC
           """)
    List<Transaction> findByAccountAndDateBetweenOrderByDateDesc(
            @Param("account") Account account,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /** Transacciones de una cuenta por categoría. */
    List<Transaction> findByAccountAndCategory(Account account, Category category);

    // ── Agregaciones para reportes mensuales ─────────────────────────────

    /**
     * Suma total de gastos de una cuenta en un período (filtrado por mes).
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.account = :account
             AND TYPE(t) = com.aureus.model.Expense
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumExpensesByAccountAndPeriod(
            @Param("account") Account account,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Suma total de ingresos de una cuenta en un período (filtrado por mes).
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.account = :account
             AND TYPE(t) = com.aureus.model.Income
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumIncomesByAccountAndPeriod(
            @Param("account") Account account,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Suma gastos de una cuenta por categoría en un período.
     * Usado por BudgetService para calcular el porcentaje de uso.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.account = :account
             AND t.category = :category
             AND TYPE(t) = com.aureus.model.Expense
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumExpensesByAccountCategoryAndPeriod(
            @Param("account") Account account,
            @Param("category") Category category,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    // ── Agregaciones de balance total (histórico, sin filtro de fecha) ────

    /**
     * Suma TODOS los ingresos de una cuenta desde su creación.
     *
     * Usado por el dashboard para mostrar el balance acumulado real
     * en lugar del balance del mes actual solamente.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.account = :account
             AND TYPE(t) = com.aureus.model.Income
           """)
    double sumTotalIncomesByAccount(@Param("account") Account account);

    /**
     * Suma TODOS los gastos de una cuenta desde su creación.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.account = :account
             AND TYPE(t) = com.aureus.model.Expense
           """)
    double sumTotalExpensesByAccount(@Param("account") Account account);
}
