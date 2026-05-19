package com.aureus.service;

import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.exception.ValidacionException;
import com.aureus.factory.TransactionFactory;
import com.aureus.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionFactory")
class TransactionFactoryTest {

    private TransactionFactory factory;
    private Account account;
    private Category category;

    @BeforeEach
    void setUp() {
        factory  = new TransactionFactory();
        User user = new User("Test", "t@test.com", "h");
        account  = new Account("Cuenta", user);
        category = new Category("Alimentación");
    }

    @Test
    @DisplayName("con tipo GASTO crea un Expense")
    void crear_conTipoGasto_creaExpense() {
        TransaccionDto dto = buildDto("GASTO", 100.0, "VARIABLE");

        Transaction t = factory.crear(dto, account, category);

        assertThat(t).isInstanceOf(Expense.class);
        assertThat(t.getAmountAsDouble()).isEqualTo(100.0);
        assertThat(((Expense) t).getTipoGasto()).isEqualTo(Expense.TipoGasto.VARIABLE);
    }

    @Test
    @DisplayName("con tipo INGRESO crea un Income")
    void crear_conTipoIngreso_creaIncome() {
        TransaccionDto dto = buildDto("INGRESO", 3500.0, null);
        dto.setSource("Salario");

        Transaction t = factory.crear(dto, account, category);

        assertThat(t).isInstanceOf(Income.class);
        assertThat(((Income) t).getSource()).isEqualTo("Salario");
    }

    @Test
    @DisplayName("con tipo desconocido lanza ValidacionException")
    void crear_conTipoDesconocido_lanzaValidacionException() {
        TransaccionDto dto = buildDto("INVERTIDO", 100.0, null);

        assertThatThrownBy(() -> factory.crear(dto, account, category))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("INVERTIDO");
    }

    @Test
    @DisplayName("con tipo en minúsculas funciona correctamente (case insensitive)")
    void crear_conTipoMinusculas_funcionaCorrectamente() {
        TransaccionDto dto = buildDto("gasto", 50.0, "FIJO");

        assertThatNoException().isThrownBy(() -> factory.crear(dto, account, category));
    }

    private TransaccionDto buildDto(String tipo, double monto, String tipoGasto) {
        TransaccionDto dto = new TransaccionDto();
        dto.setTipo(tipo);
        dto.setAmount(monto);
        dto.setDate(LocalDate.now().toString());
        dto.setAccountId(1L);
        dto.setTipoGasto(tipoGasto);
        return dto;
    }
}
