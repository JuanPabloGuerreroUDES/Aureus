package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.Budget;
import com.aureus.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Presupuesto.
 *
 * CAUSA DEL BUG (LazyInitializationException):
 *   Budget.category y Budget.account son @ManyToOne(fetch = FetchType.LAZY).
 *   Con spring.jpa.open-in-view=false, la sesión Hibernate cierra al salir
 *   del servicio. Cuando el JSP accede a ${p.category.name}, el proxy está
 *   sin sesión → LazyInitializationException.
 *
 * SOLUCIÓN: todas las queries que retornan Budget para uso en vistas
 * usan JOIN FETCH para inicializar category y account en el mismo SELECT.
 * Igual al patrón ya aplicado en TransactionRepository.
 *
 * Las queries de agregación (verificarSiSuperaPresupuesto) no necesitan JOIN
 * FETCH porque solo usan el ID del proxy para el parámetro SQL.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * FIX: JOIN FETCH carga category y account en el mismo SELECT.
     * Usado en BudgetController para mostrar la lista con ${p.category.name}.
     */
    @Query("""
           SELECT b FROM Budget b
           JOIN FETCH b.category
           JOIN FETCH b.account
           WHERE b.account = :account
           """)
    List<Budget> findByAccount(@Param("account") Account account);

    /**
     * FIX: JOIN FETCH para que el JSP pueda acceder a b.category.name
     * después de que la sesión haya cerrado (spring.jpa.open-in-view=false).
     */
    @Query("""
           SELECT b FROM Budget b
           JOIN FETCH b.category
           JOIN FETCH b.account
           WHERE b.id = :id
             AND b.account = :account
           """)
    Optional<Budget> findByIdAndAccount(
            @Param("id")      Long    id,
            @Param("account") Account account);

    /**
     * Presupuestos activos (vigentes hoy) de una cuenta — con JOIN FETCH.
     * Llamado por BudgetService.listarActivos() → BudgetController.listar()
     * → JSP budget/lista.jsp accede a ${p.category.name}.
     *
     * Sin JOIN FETCH: Hibernate carga Budget con proxies lazy para category
     * y account. Al salir de @Transactional, la sesión cierra. El JSP luego
     * llama p.category.getName() → proxy sin sesión → LazyInitializationException.
     */
    @Query("""
           SELECT b FROM Budget b
           JOIN FETCH b.category
           JOIN FETCH b.account
           WHERE b.account = :account
             AND b.startDate <= :fecha1
             AND b.endDate   >= :fecha2
           """)
    List<Budget> findByAccountAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            @Param("account") Account   account,
            @Param("fecha1")  LocalDate fecha1,
            @Param("fecha2")  LocalDate fecha2);

    /**
     * Presupuesto vigente para una cuenta + categoría en una fecha específica.
     * Usado INTERNAMENTE por verificarSiSuperaPresupuesto.
     *
     * No necesita JOIN FETCH: el resultado se usa en calcularGastoDelPresupuesto
     * que solo pasa budget.getAccount() y budget.getCategory() como parámetros
     * SQL (Hibernate usa solo el ID del proxy — no inicializa el objeto).
     * Si en el futuro se accede a category.getName() aquí, añadir JOIN FETCH.
     */
    Optional<Budget> findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Account account, Category category, LocalDate fecha1, LocalDate fecha2);
}
