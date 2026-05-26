package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.Category;
import com.aureus.model.Transaction;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ── Por cuenta (IDOR-safe) ────────────────────────────────────────────

    @Query("""
           SELECT t FROM Transaction t
           LEFT JOIN FETCH t.category
           JOIN FETCH t.account
           WHERE t.account = :account
           ORDER BY t.date DESC
           """)
    List<Transaction> findByAccountOrderByDateDesc(@Param("account") Account account);

    Optional<Transaction> findByIdAndAccount(Long id, Account account);

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

    List<Transaction> findByAccountAndCategory(Account account, Category category);

    // ── Agregaciones mensuales por cuenta ─────────────────────────────────

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account = :account AND TYPE(t) = com.aureus.model.Expense
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumExpensesByAccountAndPeriod(@Param("account") Account account,
                                         @Param("desde") LocalDate desde,
                                         @Param("hasta") LocalDate hasta);

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account = :account AND TYPE(t) = com.aureus.model.Income
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumIncomesByAccountAndPeriod(@Param("account") Account account,
                                        @Param("desde") LocalDate desde,
                                        @Param("hasta") LocalDate hasta);

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account = :account AND t.category = :category
             AND TYPE(t) = com.aureus.model.Expense
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumExpensesByAccountCategoryAndPeriod(@Param("account") Account account,
                                                  @Param("category") Category category,
                                                  @Param("desde") LocalDate desde,
                                                  @Param("hasta") LocalDate hasta);

    // ── Totales históricos por cuenta ─────────────────────────────────────

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account = :account AND TYPE(t) = com.aureus.model.Income
           """)
    double sumTotalIncomesByAccount(@Param("account") Account account);

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account = :account AND TYPE(t) = com.aureus.model.Expense
           """)
    double sumTotalExpensesByAccount(@Param("account") Account account);

    // ── Agregaciones GLOBALES: todas las cuentas del usuario ─────────────
    //
    // Usadas exclusivamente por calcularResumenGlobal() para el Dashboard.
    // Reflejan la situación financiera TOTAL del usuario, sin importar en
    // qué cuenta registró cada transacción.

    /**
     * Todos los ingresos del usuario en TODAS sus cuentas en un período.
     * t.account.user accede a la relación User a través de Account.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account.user = :user
             AND TYPE(t) = com.aureus.model.Income
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumIncomesByUserAndPeriod(@Param("user") User user,
                                     @Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta);

    /**
     * Todos los gastos del usuario en TODAS sus cuentas en un período.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account.user = :user
             AND TYPE(t) = com.aureus.model.Expense
             AND t.date BETWEEN :desde AND :hasta
           """)
    double sumExpensesByUserAndPeriod(@Param("user") User user,
                                      @Param("desde") LocalDate desde,
                                      @Param("hasta") LocalDate hasta);

    /**
     * Suma TOTAL de todos los ingresos del usuario (histórico, todas las cuentas).
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account.user = :user AND TYPE(t) = com.aureus.model.Income
           """)
    double sumTotalIncomesByUser(@Param("user") User user);

    /**
     * Suma TOTAL de todos los gastos del usuario (histórico, todas las cuentas).
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
           WHERE t.account.user = :user AND TYPE(t) = com.aureus.model.Expense
           """)
    double sumTotalExpensesByUser(@Param("user") User user);

    /**
     * Las N transacciones más recientes del usuario en todas sus cuentas.
     * Usado por el Dashboard para "Últimas transacciones" global.
     */
    @Query("""
           SELECT t FROM Transaction t
           LEFT JOIN FETCH t.category
           JOIN FETCH t.account
           WHERE t.account.user = :user
           ORDER BY t.date DESC, t.id DESC
           """)
    List<Transaction> findByUserOrderByDateDesc(@Param("user") User user);
}
