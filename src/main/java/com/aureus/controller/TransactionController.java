package com.aureus.controller;

import com.aureus.dto.transaction.TransaccionDto;
import com.aureus.model.User;
import com.aureus.repository.AccountRepository;
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

/** Controlador de Transacciones — depende de interfaces (DIP). */
@Controller
@RequestMapping("/transacciones")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService        userService;
    private final AccountRepository  accountRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) Long cuentaId,
                         @RequestParam(required = false, defaultValue = "0") int mes,
                         @RequestParam(required = false, defaultValue = "0") int anio,
                         Model model) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        model.addAttribute("cuentas",      accountRepository.findByUser(usuario));
        model.addAttribute("categorias",   categoryRepository.findAll());
        model.addAttribute("transaccionDto", new TransaccionDto());

        if (cuentaId != null) {
            LocalDate hoy = LocalDate.now();
            int m = mes  == 0 ? hoy.getMonthValue() : mes;
            int a = anio == 0 ? hoy.getYear() : anio;
            model.addAttribute("transacciones",     transactionService.listarPorMes(cuentaId, m, a, usuario));
            model.addAttribute("resumen",           transactionService.calcularResumen(cuentaId, m, a, usuario));
            model.addAttribute("cuentaSeleccionada", cuentaId);
        }
        return "transaction/lista";
    }

    @PostMapping("/nueva")
    public String registrar(@AuthenticationPrincipal UserDetails ud,
                            @Valid @ModelAttribute("transaccionDto") TransaccionDto dto,
                            BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) { ra.addFlashAttribute("errorMsg", "Datos inválidos"); return "redirect:/transacciones"; }
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
        try { transactionService.editar(id, dto, usuario); ra.addFlashAttribute("successMsg", "Actualizada"); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/transacciones?cuentaId=" + dto.getAccountId();
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @RequestParam Long cuentaId,
                           @AuthenticationPrincipal UserDetails ud, RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try { transactionService.eliminar(id, cuentaId, usuario); ra.addFlashAttribute("successMsg", "Eliminada"); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/transacciones?cuentaId=" + cuentaId;
    }
}
