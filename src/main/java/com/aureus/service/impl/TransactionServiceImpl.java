package com.aureus.service.impl;

import com.aureus.dto.report.ResumenFinancieroDto;
import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.exception.AccesoDenegadoException;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.factory.TransactionFactory;
import com.aureus.model.*;
import com.aureus.repository.AccountRepository;
import com.aureus.repository.CategoryRepository;
import com.aureus.repository.TransactionRepository;
import com.aureus.service.BudgetService;
import com.aureus.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // U8 §7.2
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository     accountRepository;
    private final CategoryRepository    categoryRepository;
    private final BudgetService         budgetService;
    private final TransactionFactory    transactionFactory;

    @Override
    @Transactional
    public Transaction registrar(TransaccionDto dto, User usuario) {
        Account  account  = obtenerCuentaDelUsuario(dto.getAccountId(), usuario);
        Category category = resolverCategoria(dto.getCategoryId());

        Transaction transaccion = transactionFactory.crear(dto, account, category);
        marcarAlertaPresupuesto(transaccion, account, category, dto);
        account.addTransaction(transaccion);

        Transaction guardada = transactionRepository.save(transaccion);
        log.info("Transacción {} registrada: id={}, monto={}", dto.getTipo(), guardada.getId(), dto.getAmount());
        return guardada;
    }

    @Override
    @Transactional
    public Transaction editar(Long transaccionId, TransaccionDto dto, User usuario) {
        Account account = obtenerCuentaDelUsuario(dto.getAccountId(), usuario);
        Transaction t   = obtenerTransaccionDeAccount(transaccionId, account);

        t.setAmount(dto.getAmount());
        t.setDate(LocalDate.parse(dto.getDate()));
        t.setDescription(dto.getDescription());
        t.setCategory(resolverCategoria(dto.getCategoryId()));
        return transactionRepository.save(t);
    }

    @Override
    @Transactional
    public void eliminar(Long transaccionId, Long cuentaId, User usuario) {
        Account     account = obtenerCuentaDelUsuario(cuentaId, usuario);
        Transaction t       = obtenerTransaccionDeAccount(transaccionId, account);
        transactionRepository.delete(t);
        log.info("Transacción id={} eliminada", transaccionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listarPorCuenta(Long cuentaId, User usuario) {
        Account account = obtenerCuentaDelUsuario(cuentaId, usuario);
        return transactionRepository.findByAccountOrderByDateDesc(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listarPorMes(Long cuentaId, int mes, int anio, User usuario) {
        Account account = obtenerCuentaDelUsuario(cuentaId, usuario);
        YearMonth ym    = YearMonth.of(anio, mes);
        return transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(
                account, ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenFinancieroDto calcularResumen(Long cuentaId, int mes, int anio, User usuario) {
        Account   account = obtenerCuentaDelUsuario(cuentaId, usuario);
        YearMonth ym      = YearMonth.of(anio, mes);
        return construirResumen(account, ym);
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private ResumenFinancieroDto construirResumen(Account account, YearMonth ym) {
        LocalDate inicio = ym.atDay(1);
        LocalDate fin    = ym.atEndOfMonth();

        double ingresos = transactionRepository.sumIncomesByAccountAndPeriod(account, inicio, fin);
        double gastos   = transactionRepository.sumExpensesByAccountAndPeriod(account, inicio, fin);

        ResumenFinancieroDto r = new ResumenFinancieroDto();
        r.setTotalIngresos(ingresos);
        r.setTotalGastos(gastos);
        r.setBalanceNeto(ingresos - gastos);
        r.setTasaAhorro(ingresos > 0 ? ((ingresos - gastos) / ingresos) * 100.0 : 0.0);
        // Locale.of() reemplaza el constructor deprecated new Locale(String,String) en Java 19+
        r.setPeriodoLabel(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "CO"))
                + " " + ym.getYear());
        return r;
    }

    private void marcarAlertaPresupuesto(Transaction t, Account account,
                                         Category category, TransaccionDto dto) {
        if (!(t instanceof Expense gasto) || category == null) return;
        boolean supera = budgetService.verificarSiSuperaPresupuesto(
                account, category, LocalDate.parse(dto.getDate()), dto.getAmount());
        gasto.setExceedsBudget(supera);
        if (supera) {
            log.warn("Gasto supera presupuesto — cuenta={}, categoría={}, monto={}",
                    account.getId(), category.getName(), dto.getAmount());
        }
    }

    private Account obtenerCuentaDelUsuario(Long cuentaId, User usuario) {
        return accountRepository.findByIdAndUser(cuentaId, usuario)
                .orElseThrow(AccesoDenegadoException::new);
    }

    private Transaction obtenerTransaccionDeAccount(Long id, Account account) {
        return transactionRepository.findByIdAndAccount(id, account)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transacción", id));
    }

    private Category resolverCategoria(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId).orElse(null);
    }
}
