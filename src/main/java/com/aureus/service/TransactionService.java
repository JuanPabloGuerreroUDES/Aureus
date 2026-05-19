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
    List<Transaction> listarPorCuenta(Long cuentaId, User usuario);
    List<Transaction> listarPorMes(Long cuentaId, int mes, int anio, User usuario);
    ResumenFinancieroDto calcularResumen(Long cuentaId, int mes, int anio, User usuario);
}
