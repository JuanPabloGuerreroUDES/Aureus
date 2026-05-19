package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.Budget;
import com.aureus.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByAccount(Account account);

    Optional<Budget> findByIdAndAccount(Long id, Account account);

    /**
     * Busca el presupuesto vigente para una cuenta y categoría en una fecha.
     * Usado por BudgetService para verificar si un gasto excede el presupuesto.
     */
    Optional<Budget> findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Account account, Category category, LocalDate fecha1, LocalDate fecha2);

    List<Budget> findByAccountAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Account account, LocalDate fecha1, LocalDate fecha2);
}
