package com.aureus.service;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.exception.AccesoDenegadoException;
import com.aureus.exception.ValidacionException;
import com.aureus.model.*;
import com.aureus.repository.AccountRepository;
import com.aureus.repository.BudgetRepository;
import com.aureus.repository.CategoryRepository;
import com.aureus.repository.TransactionRepository;
import com.aureus.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de BudgetServiceImpl (U10 §2 + §3).
 *
 * Cubre:
 *   §3.3 Stubbing — when/thenReturn para simular repositorios
 *   §3.4 ArgumentCaptor — captura el Budget guardado para verificar sus campos
 *   §2.4 @ParameterizedTest — verifica porcentajes de uso con múltiples entradas
 *   §2.5 @Nested — agrupa por escenario de negocio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetServiceImpl")
class BudgetServiceImplTest {

    @Mock private BudgetRepository      budgetRepository;
    @Mock private AccountRepository     accountRepository;
    @Mock private CategoryRepository    categoryRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    // ── Fixtures comunes ─────────────────────────────────────────────────

    private User       usuario;
    private Account    cuenta;
    private Category   categoria;
    private PresupuestoDto dto;

    @BeforeEach
    void setUp() {
        usuario   = new User("Ana", "ana@test.com", "hash");
        cuenta    = new Account("Cuenta principal", usuario);
        categoria = new Category("Alimentación", "🛒");

        dto = new PresupuestoDto();
        dto.setLimitAmount(500.0);
        dto.setStartDate(LocalDate.now().toString());
        dto.setEndDate(LocalDate.now().plusMonths(1).toString());
        dto.setCategoryId(1L);
        dto.setAccountId(1L);
        dto.setUmbralAlerta(80);
    }

    // ── §4.1 Crear presupuesto ────────────────────────────────────────────

    @Nested
    @DisplayName("Al crear un presupuesto")
    class Creacion {

        @Test
        @DisplayName("con datos válidos persiste el Budget con los campos correctos")
        void crear_conDatosValidos_persisteBudgetConCamposCorrecto() {
            // Given
            when(accountRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.of(cuenta));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoria));
            when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Budget resultado = budgetService.crear(dto, usuario);

            // Then — ArgumentCaptor captura el objeto guardado (U10 §3.4)
            ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
            verify(budgetRepository).save(captor.capture());

            Budget guardado = captor.getValue();
            assertThat(guardado.getLimitAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.0));
            assertThat(guardado.getCategory()).isEqualTo(categoria);
            assertThat(guardado.getUmbralAlertaPorcentaje()).isEqualTo(80);
        }

        @Test
        @DisplayName("con cuenta que no pertenece al usuario lanza AccesoDenegadoException")
        void crear_conCuentaAjena_lanzaAccesoDenegadoException() {
            // Given — el repositorio no encuentra la cuenta para este usuario (IDOR protection)
            when(accountRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> budgetService.crear(dto, usuario))
                    .isInstanceOf(AccesoDenegadoException.class);

            // Verificar que NUNCA se intentó guardar (U10 §3.4 — verify never)
            verify(budgetRepository, never()).save(any());
        }

        @Test
        @DisplayName("con fecha fin anterior a inicio lanza ValidacionException")
        void crear_conFechasInvertidas_lanzaValidacionException() {
            // Given — fecha de fin en el pasado
            dto.setEndDate(LocalDate.now().minusDays(1).toString());
            when(accountRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.of(cuenta));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoria));

            // When / Then
            assertThatThrownBy(() -> budgetService.crear(dto, usuario))
                    .isInstanceOf(ValidacionException.class)
                    .hasMessageContaining("posterior");
        }
    }

    // ── Calcular porcentaje de uso ─────────────────────────────────────────

    @Nested
    @DisplayName("Al calcular porcentaje de uso")
    class PorcentajeUso {

        /**
         * Prueba parametrizada (U10 §2.4): verifica que el porcentaje se calcula
         * correctamente para distintos pares (gastoActual, límite) → % esperado.
         */
        @ParameterizedTest(name = "gasto={0}, límite={1} → {2}%")
        @CsvSource({
            "0.0,   500.0,   0.0",
            "250.0, 500.0,  50.0",
            "400.0, 500.0,  80.0",
            "500.0, 500.0, 100.0",
            "600.0, 500.0, 120.0"   // gasto puede superar el límite
        })
        @DisplayName("calcula correctamente el porcentaje")
        void calcularPorcentajeUso_variosEscenarios_retornaPorcentajeCorrecto(
                double gastoActual, double limite, double esperado) {

            // Given
            Budget budget = new Budget(limite,
                    LocalDate.now(), LocalDate.now().plusMonths(1), cuenta, categoria);

            when(transactionRepository.sumExpensesByAccountCategoryAndPeriod(
                    any(), any(), any(), any()))
                    .thenReturn(gastoActual);

            // When
            double resultado = budgetService.calcularPorcentajeUso(budget);

            // Then
            assertThat(resultado).isEqualTo(esperado);
        }
    }

    // ── verificarSiSuperaPresupuesto ──────────────────────────────────────

    @Nested
    @DisplayName("Al verificar si un gasto supera el presupuesto")
    class VerificacionPresupuesto {

        @Test
        @DisplayName("cuando el gasto total supera el umbral retorna true")
        void verificarSiSupera_cuandoSuperaUmbral_retornaTrue() {
            // Given — límite 500, gasto acumulado 380, nuevo gasto 50 → total 430 = 86% > 80%
            Budget budget = new Budget(500.0,
                    LocalDate.now(), LocalDate.now().plusMonths(1), cuenta, categoria);

            when(budgetRepository.findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    any(), any(), any(), any()))
                    .thenReturn(Optional.of(budget));
            when(transactionRepository.sumExpensesByAccountCategoryAndPeriod(
                    any(), any(), any(), any()))
                    .thenReturn(380.0);  // gasto acumulado existente

            // When
            boolean supera = budgetService.verificarSiSuperaPresupuesto(
                    cuenta, categoria, LocalDate.now(), 50.0);

            // Then
            assertThat(supera).isTrue();
        }

        @Test
        @DisplayName("cuando no existe presupuesto retorna false")
        void verificarSiSupera_sinPresupuesto_retornaFalse() {
            // Given — no hay presupuesto para esta combinación cuenta+categoría
            when(budgetRepository.findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    any(), any(), any(), any()))
                    .thenReturn(Optional.empty());

            // When
            boolean supera = budgetService.verificarSiSuperaPresupuesto(
                    cuenta, categoria, LocalDate.now(), 999.0);

            // Then — sin presupuesto, nunca supera
            assertThat(supera).isFalse();
        }

        @Test
        @DisplayName("cuando el gasto está por debajo del umbral retorna false")
        void verificarSiSupera_bajoUmbral_retornaFalse() {
            // Given — límite 500, acumulado 100, nuevo 50 → total 150 = 30% < 80%
            Budget budget = new Budget(500.0,
                    LocalDate.now(), LocalDate.now().plusMonths(1), cuenta, categoria);

            when(budgetRepository.findByAccountAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    any(), any(), any(), any()))
                    .thenReturn(Optional.of(budget));
            when(transactionRepository.sumExpensesByAccountCategoryAndPeriod(
                    any(), any(), any(), any()))
                    .thenReturn(100.0);

            // When
            boolean supera = budgetService.verificarSiSuperaPresupuesto(
                    cuenta, categoria, LocalDate.now(), 50.0);

            // Then
            assertThat(supera).isFalse();
        }
    }

    // ── Eliminar presupuesto ──────────────────────────────────────────────

    @Nested
    @DisplayName("Al eliminar un presupuesto")
    class Eliminacion {

        @Test
        @DisplayName("con presupuesto existente invoca delete una vez")
        void eliminar_conPresupuestoExistente_invocaDeleteUnaSola() {
            // Given
            Budget budget = new Budget(300.0, LocalDate.now(), LocalDate.now().plusMonths(1),
                    cuenta, categoria);
            when(accountRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.of(cuenta));
            when(budgetRepository.findByIdAndAccount(10L, cuenta)).thenReturn(Optional.of(budget));

            // When
            budgetService.eliminar(10L, 1L, usuario);

            // Then — verificar que delete se llamó exactamente 1 vez con el budget correcto
            verify(budgetRepository, times(1)).delete(budget);
        }

        @Test
        @DisplayName("con presupuesto de otra cuenta lanza excepción y no borra")
        void eliminar_conPresupuestoDeOtraCuenta_noEjecutaDelete() {
            // Given — la cuenta existe para el usuario pero el budget no pertenece a ella
            when(accountRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.of(cuenta));
            when(budgetRepository.findByIdAndAccount(99L, cuenta)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> budgetService.eliminar(99L, 1L, usuario))
                    .isInstanceOf(com.aureus.exception.RecursoNoEncontradoException.class);

            verify(budgetRepository, never()).delete(any());
        }
    }
}
