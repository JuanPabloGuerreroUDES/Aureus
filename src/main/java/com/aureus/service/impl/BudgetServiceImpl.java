package com.aureus.service.impl;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.exception.AccesoDenegadoException;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.exception.ValidacionException;
import com.aureus.model.Account;
import com.aureus.model.Budget;
import com.aureus.model.Category;
import com.aureus.model.User;
import com.aureus.repository.AccountRepository;
import com.aureus.repository.BudgetRepository;
import com.aureus.repository.CategoryRepository;
import com.aureus.repository.TransactionRepository;
import com.aureus.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * CAMBIO CLAVE vs versión anterior:
 *   Se eliminó 'budget.setGastoActual()' + 'budget.checkAlert()' como
 *   dos pasos separados con estado implícito.
 *   Ahora verificarSiSuperaPresupuesto() calcula el gasto y llama
 *   budget.checkAlert(gastoTotal) en una sola operación explícita (Clean Code §3.1).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // U8 §7.2
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Budget crear(PresupuestoDto dto, User usuario) {
        Account account = obtenerCuentaDelUsuario(dto.getAccountId(), usuario);
        Category category = obtenerCategoria(dto.getCategoryId());
        validarFechas(dto.getStartDate(), dto.getEndDate());

        Budget budget = new Budget(dto.getLimitAmount(),
                LocalDate.parse(dto.getStartDate()),
                LocalDate.parse(dto.getEndDate()),
                account, category);
        budget.setUmbralAlertaPorcentaje(dto.getUmbralAlerta());
        log.info("Presupuesto creado: categoría={}, límite={}", category.getName(), dto.getLimitAmount());
        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public Budget actualizar(Long id, PresupuestoDto dto, User usuario) {
        Account account = obtenerCuentaDelUsuario(dto.getAccountId(), usuario);
        Budget budget = obtenerPresupuestoDeAccount(id, account);
        budget.setLimitAmount(dto.getLimitAmount());
        budget.setStartDate(LocalDate.parse(dto.getStartDate()));
        budget.setEndDate(LocalDate.parse(dto.getEndDate()));
        budget.setUmbralAlertaPorcentaje(dto.getUmbralAlerta());
        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long cuentaId, User usuario) {
        Account account = obtenerCuentaDelUsuario(cuentaId, usuario);
        Budget budget = obtenerPresupuestoDeAccount(id, account);
        budgetRepository.delete(budget);
        log.info("Presupuesto id={} eliminado", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> listarActivos(Long cuentaId, User usuario) {
        Account account = obtenerCuentaDelUsuario(cuentaId, usuario);
        LocalDate hoy = LocalDate.now();
        return budgetRepository
                .findByAccountAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        account, hoy, hoy);
    }

    @Override
    @Transactional(readOnly = true)
    public double calcularPorcentajeUso(Budget budget) {
        double gasto = calcularGastoDelPresupuesto(budget);
        return budget.calcularPorcentajeUso(gasto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarSiSuperaPresupuesto(Account account, Category category,
                                                LocalDate fecha, double montoNuevoGasto) {
        return budgetRepository
                .findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        account, category, fecha, fecha)
                .map(budget -> {
                    double gastoActual = calcularGastoDelPresupuesto(budget);
                    double gastoTotal  = gastoActual + montoNuevoGasto;
                    // checkAlert recibe el valor calculado — sin estado implícito
                    return budget.checkAlert(gastoTotal);
                })
                .orElse(false);
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private double calcularGastoDelPresupuesto(Budget budget) {
        return transactionRepository.sumExpensesByAccountCategoryAndPeriod(
                budget.getAccount(), budget.getCategory(),
                budget.getStartDate(), budget.getEndDate());
    }

    private Account obtenerCuentaDelUsuario(Long cuentaId, User usuario) {
        return accountRepository.findByIdAndUser(cuentaId, usuario)
                .orElseThrow(AccesoDenegadoException::new);
    }

    private Category obtenerCategoria(Long categoriaId) {
        return categoryRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", categoriaId));
    }

    private Budget obtenerPresupuestoDeAccount(Long budgetId, Account account) {
        return budgetRepository.findByIdAndAccount(budgetId, account)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto", budgetId));
    }

    private void validarFechas(String startDate, String endDate) {
        LocalDate inicio = LocalDate.parse(startDate);
        LocalDate fin    = LocalDate.parse(endDate);
        if (fin.isBefore(inicio)) {
            throw new ValidacionException("La fecha de fin debe ser posterior a la de inicio");
        }
    }
}
