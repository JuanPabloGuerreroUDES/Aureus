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

    /** Todas las transacciones de una cuenta, ordenadas por fecha desc. */
    List<Transaction> findByAccountOrderByDateDesc(Account account);

    /** Busca transacción por ID y cuenta (protección IDOR). */
    Optional<Transaction> findByIdAndAccount(Long id, Account account);

    /**
     * Transacciones de una cuenta en un rango de fechas.
     * Usado para reportes mensuales y simulaciones.
     */
    List<Transaction> findByAccountAndDateBetweenOrderByDateDesc(
            Account account, LocalDate desde, LocalDate hasta);

    /**
     * Transacciones de una cuenta por categoría.
     */
    List<Transaction> findByAccountAndCategory(Account account, Category category);

    // ── Agregaciones para reportes ────────────────────────────────────────

    /**
     * Suma total de gastos (tipo GASTO) de una cuenta en un período.
     * Usa JPQL con TYPE() para filtrar por subclase.
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
     * Suma total de ingresos (tipo INGRESO) de una cuenta en un período.
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
}
