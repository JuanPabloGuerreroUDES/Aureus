package com.aureus.controller;

import com.aureus.model.Account;
import com.aureus.model.User;
import com.aureus.repository.AccountRepository;
import com.aureus.service.SavingsGoalService;
import com.aureus.service.TransactionService;
import com.aureus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador del Dashboard principal.
 *
 * Muestra el resumen financiero del usuario autenticado:
 *   - KPIs del mes (ingresos, gastos, balance, ahorro)
 *   - Últimas transacciones
 *   - Metas activas
 *   - Alertas de presupuesto
 *
 * @AuthenticationPrincipal inyecta el UserDetails del usuario autenticado
 * directamente en el método, sin necesidad de acceder al SecurityContextHolder.
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final SavingsGoalService savingsGoalService;
    private final AccountRepository accountRepository;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Carga el usuario completo de la BD (UserDetails solo tiene email y rol)
        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        List<Account> cuentas = accountRepository.findByUser(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("metas", savingsGoalService.listarPorUsuario(usuario));

        // Resumen del mes actual
        if (!cuentas.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            model.addAttribute("resumen",
                    transactionService.calcularResumen(
                            cuentas.get(0).getId(), hoy.getMonthValue(), hoy.getYear(), usuario));
            model.addAttribute("ultimasTransacciones",
                    transactionService.listarPorCuenta(cuentas.get(0).getId(), usuario)
                            .stream().limit(5).toList());
        }

        return "dashboard/index"; // → /WEB-INF/views/dashboard/index.jsp
    }
}
