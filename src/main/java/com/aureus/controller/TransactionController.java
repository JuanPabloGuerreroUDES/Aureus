package com.aureus.controller;

import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.model.User;
import com.aureus.repository.CategoryRepository;
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

@Controller
@RequestMapping("/transacciones")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService        userService;
    private final CategoryRepository categoryRepository;

    /**
     * FIX: El JSP envía 'periodo=YYYY-MM' (input type="month").
     * El controller anterior esperaba 'mes' y 'anio' como parámetros separados.
     * Ahora acepta 'periodo' y también los params individuales como fallback.
     */
    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) Long cuentaId,
                         @RequestParam(required = false) String periodo,
                         @RequestParam(required = false, defaultValue = "0") int mes,
                         @RequestParam(required = false, defaultValue = "0") int anio,
                         Model model) {

        User usuario = userService.buscarPorEmail(ud.getUsername());
        model.addAttribute("cuentas",        userService.listarCuentas(usuario));
        model.addAttribute("categorias",     categoryRepository.findAll());
        model.addAttribute("transaccionDto", new TransaccionDto());

        // FIX: parsear 'periodo' (YYYY-MM) si viene del input type="month"
        if (periodo != null && !periodo.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(periodo);
                mes  = ym.getMonthValue();
                anio = ym.getYear();
            } catch (Exception ignored) {}
        }

        // Defaults al mes actual si no se especificaron
        LocalDate hoy = LocalDate.now();
        if (mes  == 0) mes  = hoy.getMonthValue();
        if (anio == 0) anio = hoy.getYear();

        // El periodo actual para preseleccionar el input
        model.addAttribute("periodoActual", YearMonth.of(anio, mes).toString()); // "YYYY-MM"

        if (cuentaId != null) {
            model.addAttribute("transacciones",      transactionService.listarPorMes(cuentaId, mes, anio, usuario));
            model.addAttribute("resumen",            transactionService.calcularResumen(cuentaId, mes, anio, usuario));
            model.addAttribute("cuentaSeleccionada", cuentaId);
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
}
