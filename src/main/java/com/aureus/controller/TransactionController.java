package com.aureus.controller;

import com.aureus.dto.report.ResumenFinancieroDto;
import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.model.Account;
import com.aureus.model.User;
import com.aureus.repository.CategoryRepository;
import com.aureus.service.AccountService;
import com.aureus.service.TransactionService;
import com.aureus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/transacciones")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService        userService;
    private final AccountService     accountService;
    private final CategoryRepository categoryRepository;

    /**
     * Regla de datos según cuenta seleccionada:
     *
     *   Cuenta PRINCIPAL → datos GLOBALES (todas las cuentas del usuario).
     *     • resumenTotal = calcularResumenGlobal()
     *     • transacciones = listarTodasPorUsuario()
     *     • Al filtrar por mes → calcularResumenMensualGlobal()
     *
     *   Cuenta adicional → datos SOLO de esa cuenta.
     *     • resumenTotal = calcularResumenCompleto(cuentaId)
     *     • transacciones = listarPorCuenta(cuentaId)
     *     • Al filtrar por mes → calcularResumen(cuentaId, mes, anio)
     */
    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) Long cuentaId,
                         @RequestParam(required = false) String periodo,
                         Model model) {

        User          usuario = userService.buscarPorEmail(ud.getUsername());
        List<Account> cuentas = accountService.listar(usuario);

        Account cuentaActual = (cuentaId != null)
                ? cuentas.stream().filter(c -> c.getId().equals(cuentaId)).findFirst()
                         .orElse(accountService.obtenerPrincipal(usuario))
                : accountService.obtenerPrincipal(usuario);

        boolean esPrincipal = cuentaActual.isEsPrincipal();

        model.addAttribute("cuentas",            cuentas);
        model.addAttribute("categorias",         categoryRepository.findAll());
        model.addAttribute("transaccionDto",     new TransaccionDto());
        model.addAttribute("cuentaSeleccionada", cuentaActual.getId());
        model.addAttribute("periodoActual",      periodo != null ? periodo : "");
        model.addAttribute("esPrincipal",        esPrincipal);

        // Balance total acumulado: global si es principal, de la cuenta si es adicional
        ResumenFinancieroDto resumenTotal = esPrincipal
                ? transactionService.calcularResumenGlobal(usuario)
                : transactionService.calcularResumenCompleto(cuentaActual.getId(), usuario);
        model.addAttribute("resumenTotal", resumenTotal);

        if (periodo != null && !periodo.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(periodo);
                // KPIs del período filtrado: globales o por cuenta
                ResumenFinancieroDto resumenPeriodo = esPrincipal
                        ? transactionService.calcularResumenMensualGlobal(usuario, ym.getMonthValue(), ym.getYear())
                        : transactionService.calcularResumen(cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario);

                // Transacciones del período filtrado
                model.addAttribute("transacciones", esPrincipal
                        ? transactionService.listarTodasPorUsuario(usuario).stream()
                            .filter(t -> {
                                YearMonth txMes = YearMonth.from(t.getDate());
                                return txMes.equals(ym);
                            }).toList()
                        : transactionService.listarPorMes(cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario));

                model.addAttribute("resumen",      resumenPeriodo);
                model.addAttribute("modoFiltrado", true);
            } catch (Exception ignored) {
                cargarTodas(cuentaActual, esPrincipal, usuario, model, resumenTotal);
            }
        } else {
            cargarTodas(cuentaActual, esPrincipal, usuario, model, resumenTotal);
        }

        return "transaction/lista";
    }

    @PostMapping("/nueva")
    public String registrar(@AuthenticationPrincipal UserDetails ud,
                            @Valid @ModelAttribute("transaccionDto") TransaccionDto dto,
                            BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Datos inválidos");
            return "redirect:/transacciones?cuentaId=" + dto.getAccountId();
        }
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            transactionService.registrar(dto, usuario);
            ra.addFlashAttribute("successMsg", "Transacción registrada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/transacciones?cuentaId=" + dto.getAccountId();
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud,
                         @ModelAttribute TransaccionDto dto, RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try { transactionService.editar(id, dto, usuario); ra.addFlashAttribute("successMsg", "Transacción actualizada"); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/transacciones?cuentaId=" + dto.getAccountId();
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @RequestParam Long cuentaId,
                           @AuthenticationPrincipal UserDetails ud, RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try { transactionService.eliminar(id, cuentaId, usuario); ra.addFlashAttribute("successMsg", "Transacción eliminada"); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/transacciones?cuentaId=" + cuentaId;
    }

    private void cargarTodas(Account cuenta, boolean esPrincipal, User usuario,
                              Model model, ResumenFinancieroDto resumenTotal) {
        model.addAttribute("transacciones", esPrincipal
                ? transactionService.listarTodasPorUsuario(usuario)
                : transactionService.listarPorCuenta(cuenta.getId(), usuario));
        model.addAttribute("resumen",       resumenTotal);
        model.addAttribute("modoFiltrado",  false);
    }
}
