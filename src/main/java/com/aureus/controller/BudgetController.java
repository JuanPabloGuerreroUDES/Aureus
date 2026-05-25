package com.aureus.controller;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.model.Account;
import com.aureus.model.Budget;
import com.aureus.model.User;
import com.aureus.repository.CategoryRepository;
import com.aureus.service.AccountService;
import com.aureus.service.BudgetService;
import com.aureus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/presupuestos")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService      budgetService;
    private final UserService        userService;
    private final AccountService     accountService;
    private final CategoryRepository categoryRepository;

    /**
     * Muestra los presupuestos activos.
     *
     * Por defecto carga los presupuestos de la cuenta principal automáticamente.
     * El usuario puede cambiar de cuenta usando el selector.
     */
    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam(required = false) Long cuentaId,
                         Model model) {

        User usuario          = userService.buscarPorEmail(ud.getUsername());
        List<Account> cuentas = accountService.listar(usuario);

        // Auto-selección de cuenta principal si no se especificó
        Account cuentaActual = (cuentaId != null)
                ? cuentas.stream().filter(c -> c.getId().equals(cuentaId)).findFirst()
                         .orElse(accountService.obtenerPrincipal(usuario))
                : accountService.obtenerPrincipal(usuario);

        List<Budget> presupuestos = budgetService.listarActivos(cuentaActual.getId(), usuario);

        model.addAttribute("cuentas",            cuentas);
        model.addAttribute("cuentaActual",       cuentaActual);
        model.addAttribute("cuentaSeleccionada", cuentaActual.getId());
        model.addAttribute("categorias",         categoryRepository.findAll());
        model.addAttribute("presupuestoDto",     new PresupuestoDto());
        model.addAttribute("presupuestos",       presupuestos);
        model.addAttribute("porcentajesUso",
            presupuestos.stream().map(budgetService::calcularPorcentajeUso).toList());

        return "budget/lista";
    }

    @PostMapping("/nuevo")
    public String crear(@AuthenticationPrincipal UserDetails ud,
                        @ModelAttribute PresupuestoDto dto,
                        RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            budgetService.crear(dto, usuario);
            ra.addFlashAttribute("successMsg", "Presupuesto creado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/presupuestos?cuentaId=" + dto.getAccountId();
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud,
                         @ModelAttribute PresupuestoDto dto, RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            budgetService.actualizar(id, dto, usuario);
            ra.addFlashAttribute("successMsg", "Presupuesto actualizado");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/presupuestos?cuentaId=" + dto.getAccountId();
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @RequestParam Long cuentaId,
                           @AuthenticationPrincipal UserDetails ud, RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            budgetService.eliminar(id, cuentaId, usuario);
            ra.addFlashAttribute("successMsg", "Presupuesto eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/presupuestos?cuentaId=" + cuentaId;
    }
}
