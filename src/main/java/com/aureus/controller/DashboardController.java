package com.aureus.controller;

import com.aureus.model.Account;
import com.aureus.model.User;
import com.aureus.service.AccountService;
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

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService        userService;
    private final AccountService     accountService;
    private final TransactionService transactionService;
    private final SavingsGoalService savingsGoalService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User          usuario = userService.buscarPorEmail(userDetails.getUsername());
        List<Account> cuentas = accountService.listar(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("metas",   savingsGoalService.listarPorUsuario(usuario));

        if (!cuentas.isEmpty()) {
            // El dashboard siempre muestra la visión GLOBAL:
            // balance total y KPIs del mes de TODAS las cuentas del usuario.
            model.addAttribute("resumen",
                    transactionService.calcularResumenGlobal(usuario));
            // Últimas 5 transacciones de TODAS las cuentas
            model.addAttribute("ultimasTransacciones",
                    transactionService.listarRecientesPorUsuario(usuario, 5));
        }

        return "dashboard/index";
    }
}
