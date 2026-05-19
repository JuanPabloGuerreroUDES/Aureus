package com.aureus.service;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.model.Account;
import com.aureus.model.Budget;
import com.aureus.model.Category;
import com.aureus.model.User;

import java.time.LocalDate;
import java.util.List;

public interface BudgetService {
    Budget crear(PresupuestoDto dto, User usuario);
    Budget actualizar(Long id, PresupuestoDto dto, User usuario);
    void eliminar(Long id, Long cuentaId, User usuario);
    List<Budget> listarActivos(Long cuentaId, User usuario);
    double calcularPorcentajeUso(Budget budget);
    boolean verificarSiSuperaPresupuesto(Account account, Category category,
                                         LocalDate fecha, double montoNuevoGasto);
}
