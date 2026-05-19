package com.aureus.factory;

import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.exception.ValidacionException;
import com.aureus.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Factory para crear objetos Transacción (U11 §4.4 - Patrón Factory).
 *
 * CORRECCIÓN: Se eliminó el uso de BigDecimal como intermediario.
 * Motivo: Lombok omite la generación de setAmount(BigDecimal) cuando ya
 * existe un método setAmount(double) con el mismo nombre, independientemente
 * de la firma. Al pasar double directamente, Transaction.setAmount(double)
 * hace la conversión a BigDecimal internamente, sin ambigüedad de tipos.
 */
@Component
public class TransactionFactory {

    public Transaction crear(TransaccionDto dto, Account account, Category category) {
        LocalDate fecha = LocalDate.parse(dto.getDate());
        double    monto = dto.getAmount();  // double — Transaction convierte a BigDecimal

        return switch (dto.getTipo().toUpperCase()) {
            case "GASTO"   -> crearGasto(dto, account, category, fecha, monto);
            case "INGRESO" -> crearIngreso(dto, account, category, fecha, monto);
            default -> throw new ValidacionException(
                    "Tipo de transacción no reconocido: " + dto.getTipo()
                    + ". Valores válidos: GASTO, INGRESO");
        };
    }

    private Expense crearGasto(TransaccionDto dto, Account account,
                               Category category, LocalDate fecha, double monto) {
        Expense gasto = new Expense();
        gasto.setAmount(monto);
        gasto.setDate(fecha);
        gasto.setDescription(dto.getDescription());
        gasto.setAccount(account);
        gasto.setCategory(category);
        gasto.setRecurring(dto.isRecurring());
        gasto.setTipoGasto(parseTipoGasto(dto.getTipoGasto()));
        return gasto;
    }

    private Income crearIngreso(TransaccionDto dto, Account account,
                                Category category, LocalDate fecha, double monto) {
        Income ingreso = new Income();
        ingreso.setAmount(monto);
        ingreso.setDate(fecha);
        ingreso.setDescription(dto.getDescription());
        ingreso.setAccount(account);
        ingreso.setCategory(category);
        ingreso.setRecurring(dto.isRecurring());
        ingreso.setSource(dto.getSource());
        return ingreso;
    }

    private Expense.TipoGasto parseTipoGasto(String tipoGasto) {
        if (tipoGasto == null || tipoGasto.isBlank()) return Expense.TipoGasto.VARIABLE;
        try {
            return Expense.TipoGasto.valueOf(tipoGasto.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Expense.TipoGasto.VARIABLE;
        }
    }
}
