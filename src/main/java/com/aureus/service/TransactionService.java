package com.aureus.service;

import com.aureus.dto.report.ResumenFinancieroDto;
import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.model.Transaction;
import com.aureus.model.User;

import java.util.List;

public interface TransactionService {

    Transaction registrar(TransaccionDto dto, User usuario);
    Transaction editar(Long id, TransaccionDto dto, User usuario);
    void eliminar(Long transaccionId, Long cuentaId, User usuario);

    // ── Por cuenta específica ────────────────────────────────────────────

    List<Transaction> listarPorCuenta(Long cuentaId, User usuario);
    List<Transaction> listarPorMes(Long cuentaId, int mes, int anio, User usuario);
    ResumenFinancieroDto calcularResumen(Long cuentaId, int mes, int anio, User usuario);
    ResumenFinancieroDto calcularResumenCompleto(Long cuentaId, User usuario);

    // ── Global: TODAS las cuentas del usuario ────────────────────────────

    /**
     * Todas las transacciones del usuario en todas sus cuentas, ordenadas por fecha.
     * Usado por el módulo Transacciones cuando está seleccionada la cuenta principal.
     */
    List<Transaction> listarTodasPorUsuario(User usuario);

    /**
     * Las N transacciones más recientes del usuario en todas sus cuentas.
     * Usado por el Dashboard.
     */
    List<Transaction> listarRecientesPorUsuario(User usuario, int limite);

    /**
     * Resumen global (mes actual + histórico) de TODAS las cuentas del usuario.
     * Usado por el Dashboard y la cuenta principal.
     */
    ResumenFinancieroDto calcularResumenGlobal(User usuario);

    /**
     * Resumen de un mes específico de TODAS las cuentas del usuario.
     * Usado por Transacciones y Reportes cuando la cuenta principal está seleccionada
     * y el usuario aplica un filtro de mes.
     */
    ResumenFinancieroDto calcularResumenMensualGlobal(User usuario, int mes, int anio);
}
