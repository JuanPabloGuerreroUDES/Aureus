package com.aureus.controller;

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

import java.time.LocalDate;
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
     * Lista de transacciones.
     *
     * Comportamiento por defecto (sin parámetros):
     *   - Cuenta: principal del usuario (automático, sin selección requerida)
     *   - Período: TODAS las transacciones (sin filtro de mes)
     *
     * El usuario puede optar por filtrar por mes usando el selector de período.
     * Si elige otro mes, la URL incluye ?periodo=YYYY-MM y ?cuentaId=X.
     *
     * @param cuentaId  opcional — si no viene, se usa la cuenta principal
     * @param periodo   opcional — formato YYYY-MM; si no viene, se muestran todas
     */
    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) Long cuentaId,
                         @RequestParam(required = false) String periodo,
                         Model model) {

        User usuario          = userService.buscarPorEmail(ud.getUsername());
        List<Account> cuentas = accountService.listar(usuario);

        // Auto-selección de cuenta principal si no se especificó
        Account cuentaActual = (cuentaId != null)
                ? cuentas.stream().filter(c -> c.getId().equals(cuentaId)).findFirst()
                         .orElse(accountService.obtenerPrincipal(usuario))
                : accountService.obtenerPrincipal(usuario);

        model.addAttribute("cuentas",            cuentas);
        model.addAttribute("categorias",         categoryRepository.findAll());
        model.addAttribute("transaccionDto",     new TransaccionDto());
        model.addAttribute("cuentaSeleccionada", cuentaActual.getId());
        model.addAttribute("periodoActual",      periodo != null ? periodo : "");

        if (periodo != null && !periodo.isBlank()) {
            // Modo filtrado por mes
            try {
                YearMonth ym = YearMonth.parse(periodo);
                model.addAttribute("transacciones",
                    transactionService.listarPorMes(cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario));
                model.addAttribute("resumen",
                    transactionService.calcularResumen(cuentaActual.getId(), ym.getMonthValue(), ym.getYear(), usuario));
                model.addAttribute("modoFiltrado", true);
            } catch (Exception ignored) {
                // Si el periodo tiene mal formato, mostrar todas
                cargarTodasLasTransacciones(cuentaActual, usuario, model);
            }
        } else {
            // Modo completo: todas las transacciones + balance total
            cargarTodasLasTransacciones(cuentaActual, usuario, model);
        }

        return "transaction/lista";
    }

    @PostMapping("/nueva")
    public String registrar(@AuthenticationPrincipal UserDetails ud,
                            @Valid @ModelAttribute("transaccionDto") TransaccionDto dto,
                            BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Datos inválidos: " +
                result.getFieldErrors().stream()
                    .map(e -> e.getField() + " " + e.getDefaultMessage())
                    .reduce("", (a, b) -> a + "; " + b));
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

    private void cargarTodasLasTransacciones(Account cuenta, User usuario, Model model) {
        model.addAttribute("transacciones",
            transactionService.listarPorCuenta(cuenta.getId(), usuario));
        model.addAttribute("resumen",
            transactionService.calcularResumenCompleto(cuenta.getId(), usuario));
        model.addAttribute("modoFiltrado", false);
    }
}
