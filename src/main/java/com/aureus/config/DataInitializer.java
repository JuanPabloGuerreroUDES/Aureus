package com.aureus.config;

import com.aureus.model.*;
import com.aureus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Inicializador de datos de desarrollo.
 *
 * REFACTORIZACIÓN (U11 §3.2 - Funciones Pequeñas y Enfocadas):
 *   La función run() original tenía 80+ líneas haciendo todo.
 *   Ahora run() orquesta y delega a métodos privados de ≤20 líneas c/u.
 *   Cada método tiene UNA responsabilidad (SRP §2.1).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository         userRepository;
    private final CategoryRepository     categoryRepository;
    private final AccountRepository      accountRepository;
    private final TransactionRepository  transactionRepository;
    private final SavingsGoalRepository  savingsGoalRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Inicializando datos de desarrollo ===");
        List<Category> categorias = inicializarCategorias();
        User admin = inicializarUsuario("Admin Aureus", "admin@aureus.com", "Admin2026!", "ROLE_ADMIN");
        User demo  = inicializarUsuario("Juan García",   "demo@aureus.com",  "Demo2026!",  "ROLE_USER");
        Account cuenta = inicializarCuenta(demo);
        inicializarTransacciones(cuenta, categorias);
        inicializarMetas(demo);
        log.info("=== Datos listos | demo: demo@aureus.com / Demo2026! | admin: admin@aureus.com / Admin2026! ===");
    }

    // ── Un método por responsabilidad ─────────────────────────────────────

    private List<Category> inicializarCategorias() {
        String[][] cats = {
            {"Alimentación","🛒"}, {"Transporte","🚗"}, {"Entretenimiento","🎬"},
            {"Salud","🏥"}, {"Educación","📚"}, {"Ropa","👕"},
            {"Servicios","💡"}, {"Salario","💼"}, {"Freelance","💻"},
            {"Inversión","📈"}, {"Otros","📦"}
        };
        return Arrays.stream(cats)
                .filter(c -> !categoryRepository.existsByName(c[0]))
                .map(c -> categoryRepository.save(new Category(c[0], c[1])))
                .peek(c -> log.debug("Categoría creada: {}", c.getName()))
                .toList();
    }

    private User inicializarUsuario(String nombre, String email, String password, String rol) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(nombre, email, passwordEncoder.encode(password));
            u.setRol(rol);
            User guardado = userRepository.save(u);
            log.info("Usuario creado: {} ({})", email, rol);
            return guardado;
        });
    }

    private Account inicializarCuenta(User usuario) {
        List<Account> existentes = accountRepository.findByUser(usuario);
        if (!existentes.isEmpty()) return existentes.get(0);
        Account cuenta = accountRepository.save(new Account("Cuenta principal", usuario));
        log.info("Cuenta creada para {}", usuario.getEmail());
        return cuenta;
    }

    private void inicializarTransacciones(Account cuenta, List<Category> categorias) {
        if (!transactionRepository.findByAccountOrderByDateDesc(cuenta).isEmpty()) return;

        Category salario      = getCat(categorias, "Salario");
        Category freelance    = getCat(categorias, "Freelance");
        Category alimentacion = getCat(categorias, "Alimentación");
        Category transporte   = getCat(categorias, "Transporte");
        Category entreten     = getCat(categorias, "Entretenimiento");

        LocalDate hoy = LocalDate.now();

        List<Transaction> txs = List.of(
            buildIngreso(BigDecimal.valueOf(3500), hoy.withDayOfMonth(1), "Salario mensual", cuenta, salario, "Salario", true),
            buildIngreso(BigDecimal.valueOf(700),  hoy.withDayOfMonth(10), "Proyecto diseño web", cuenta, freelance, "Freelance", false),
            buildGasto(BigDecimal.valueOf(450),  hoy.withDayOfMonth(5),  "Supermercado Éxito", cuenta, alimentacion, Expense.TipoGasto.VARIABLE),
            buildGasto(BigDecimal.valueOf(120),  hoy.withDayOfMonth(8),  "Gasolina y transporte", cuenta, transporte, Expense.TipoGasto.VARIABLE),
            buildGasto(BigDecimal.valueOf(17),   hoy.withDayOfMonth(12), "Netflix mensual", cuenta, entreten, Expense.TipoGasto.FIJO)
        );

        txs.forEach(t -> { cuenta.addTransaction(t); transactionRepository.save(t); });
        log.info("Transacciones de ejemplo creadas para cuenta id={}", cuenta.getId());
    }

    private void inicializarMetas(User usuario) {
        if (!savingsGoalRepository.findByUser(usuario).isEmpty()) return;

        SavingsGoal meta1 = new SavingsGoal("Fondo de emergencia", 5000, LocalDate.now().plusMonths(7), usuario);
        meta1.registrarAporte(3250);
        SavingsGoal meta2 = new SavingsGoal("Viaje a Europa", 3000, LocalDate.now().plusMonths(15), usuario);
        meta2.registrarAporte(840);

        savingsGoalRepository.saveAll(List.of(meta1, meta2));
        log.info("Metas de ejemplo creadas para {}", usuario.getEmail());
    }

    // ── Builders privados (reducen duplicación) ───────────────────────────

    private Income buildIngreso(BigDecimal monto, LocalDate fecha, String desc,
                                 Account cuenta, Category cat, String fuente, boolean recurrente) {
        Income i = new Income(monto.doubleValue(), fecha, desc, cuenta, cat, fuente);
        i.setRecurring(recurrente);
        return i;
    }

    private Expense buildGasto(BigDecimal monto, LocalDate fecha, String desc,
                                Account cuenta, Category cat, Expense.TipoGasto tipo) {
        return new Expense(monto.doubleValue(), fecha, desc, cuenta, cat, tipo);
    }

    private Category getCat(List<Category> lista, String nombre) {
        return lista.stream().filter(c -> c.getName().equals(nombre)).findFirst()
                .or(() -> categoryRepository.findByName(nombre))
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada: " + nombre));
    }
}
