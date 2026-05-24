package com.aureus.repository;

import com.aureus.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de repositorio con @DataJpaTest (U10 §4.3).
 *
 * @DataJpaTest carga solo la capa de persistencia:
 *   - Entidades JPA y sus repositorios.
 *   - H2 en memoria (definido en application-test.properties).
 *   - Las transacciones se revierten automáticamente al terminar cada prueba,
 *     garantizando aislamiento total entre tests.
 *
 * TestEntityManager es el helper de Spring para persistir datos de prueba
 * sin pasar por la lógica del servicio.
 *
 * Se prueban las consultas JPQL personalizadas de TransactionRepository
 * que usan TYPE() y COALESCE(), no cubiertos por pruebas unitarias.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("TransactionRepository — JPQL custom queries")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager em;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private User     usuario;
    private Account  cuenta;
    private Category catAlimentacion;
    private Category catTransporte;

    @BeforeEach
    void setUp() {
        // Persistir datos base sin IDs explícitos (JPA los asigna)
        usuario        = em.persistAndFlush(new User("Test", "test@test.com", "hash"));
        cuenta         = em.persistAndFlush(new Account("Principal", usuario));
        catAlimentacion = em.persistAndFlush(new Category("Alimentación", "🛒"));
        catTransporte   = em.persistAndFlush(new Category("Transporte", "🚗"));
    }

    // ── sumExpensesByAccountAndPeriod ─────────────────────────────────────

    @Nested
    @DisplayName("sumExpensesByAccountAndPeriod")
    class SumaGastos {

        @Test
        @DisplayName("con gastos en el período retorna la suma correcta")
        void sumExpenses_conGastosEnPeriodo_retornaSumacorrecta() {
            // Given
            LocalDate hoy = LocalDate.now();
            persistirGasto(200.0, hoy, catAlimentacion);
            persistirGasto(150.0, hoy, catTransporte);
            persistirIngreso(1000.0, hoy, catAlimentacion);  // NO debe sumarse

            // When
            double total = transactionRepository.sumExpensesByAccountAndPeriod(
                    cuenta, hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));

            // Then
            assertThat(total).isEqualTo(350.0);
        }

        @Test
        @DisplayName("sin gastos en el período retorna 0.0 (COALESCE)")
        void sumExpenses_sinGastos_retornaCero() {
            // Given — solo ingresos, sin gastos
            persistirIngreso(500.0, LocalDate.now(), catAlimentacion);

            // When
            double total = transactionRepository.sumExpensesByAccountAndPeriod(
                    cuenta, LocalDate.now().minusMonths(1), LocalDate.now());

            // Then — COALESCE(SUM(...), 0) debe retornar 0, no null
            assertThat(total).isZero();
        }

        @Test
        @DisplayName("gastos fuera del período no se incluyen")
        void sumExpenses_gastosFueraDePeriodo_noSeIncluyen() {
            // Given — gasto del mes pasado
            LocalDate mesPasado = LocalDate.now().minusMonths(1);
            persistirGasto(999.0, mesPasado, catAlimentacion);

            // When — período: solo este mes
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes    = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            double total = transactionRepository.sumExpensesByAccountAndPeriod(
                    cuenta, inicioMes, finMes);

            // Then
            assertThat(total).isZero();
        }
    }

    // ── sumIncomesByAccountAndPeriod ──────────────────────────────────────

    @Nested
    @DisplayName("sumIncomesByAccountAndPeriod")
    class SumaIngresos {

        @Test
        @DisplayName("con ingresos en el período retorna la suma correcta")
        void sumIncomes_conIngresosEnPeriodo_retornaSumacorrecta() {
            // Given
            LocalDate hoy = LocalDate.now();
            persistirIngreso(3500.0, hoy, catAlimentacion);
            persistirIngreso(700.0,  hoy, catTransporte);
            persistirGasto(100.0, hoy, catAlimentacion);  // NO debe sumarse

            // When
            double total = transactionRepository.sumIncomesByAccountAndPeriod(
                    cuenta, hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));

            // Then
            assertThat(total).isEqualTo(4200.0);
        }
    }

    // ── sumExpensesByAccountCategoryAndPeriod ─────────────────────────────

    @Nested
    @DisplayName("sumExpensesByAccountCategoryAndPeriod")
    class SumaGastosPorCategoria {

        @Test
        @DisplayName("filtra correctamente por categoría — solo suma gastos de esa categoría")
        void sumExpensesByCategory_conMultiplesCategorias_filtraCorrectamente() {
            // Given
            LocalDate hoy = LocalDate.now();
            persistirGasto(300.0, hoy, catAlimentacion);
            persistirGasto(100.0, hoy, catTransporte);    // diferente categoría
            persistirGasto(200.0, hoy, catAlimentacion);

            // When — buscar solo Alimentación
            double total = transactionRepository.sumExpensesByAccountCategoryAndPeriod(
                    cuenta, catAlimentacion,
                    hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));

            // Then — 300 + 200 = 500, excluye los 100 de Transporte
            assertThat(total).isEqualTo(500.0);
        }
    }

    // ── findByAccountOrderByDateDesc ──────────────────────────────────────

    @Nested
    @DisplayName("findByAccountOrderByDateDesc")
    class ConsultaPorCuenta {

        @Test
        @DisplayName("retorna transacciones ordenadas por fecha descendente")
        void findByAccount_retornaOrdenadoPorFechaDesc() {
            // Given
            LocalDate hoy     = LocalDate.now();
            LocalDate ayer    = hoy.minusDays(1);
            LocalDate anteayer = hoy.minusDays(2);

            persistirGasto(100.0, ayer,    catAlimentacion);
            persistirGasto(200.0, hoy,     catAlimentacion);
            persistirGasto(50.0,  anteayer, catTransporte);

            // When
            List<Transaction> resultado =
                    transactionRepository.findByAccountOrderByDateDesc(cuenta);

            // Then — primero la más reciente (hoy)
            assertThat(resultado).hasSize(3);
            assertThat(resultado.get(0).getDate()).isEqualTo(hoy);
            assertThat(resultado.get(2).getDate()).isEqualTo(anteayer);
        }

        @Test
        @DisplayName("findByIdAndAccount protege contra IDOR — no retorna tx de otra cuenta")
        void findByIdAndAccount_txDeOtraCuenta_retornaEmpty() {
            // Given — otra cuenta del mismo usuario
            Account otraCuenta = em.persistAndFlush(new Account("Otra", usuario));
            Expense gasto = new Expense(50.0, LocalDate.now(), "test", otraCuenta, catAlimentacion, Expense.TipoGasto.VARIABLE);
            em.persistAndFlush(gasto);

            // When — intenta acceder desde 'cuenta' (la cuenta incorrecta)
            Optional<Transaction> resultado =
                    transactionRepository.findByIdAndAccount(gasto.getId(), cuenta);

            // Then — debe retornar vacío (protección IDOR)
            assertThat(resultado).isEmpty();
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private void persistirGasto(double monto, LocalDate fecha, Category cat) {
        Expense e = new Expense(monto, fecha, "Gasto test", cuenta, cat, Expense.TipoGasto.VARIABLE);
        em.persistAndFlush(e);
    }

    private void persistirIngreso(double monto, LocalDate fecha, Category cat) {
        Income i = new Income(monto, fecha, "Ingreso test", cuenta, cat, "Salario");
        em.persistAndFlush(i);
    }
}
