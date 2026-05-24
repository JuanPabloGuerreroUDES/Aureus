package com.aureus.controller;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.model.User;
import com.aureus.repository.CategoryRepository;
import com.aureus.service.BudgetService;
import com.aureus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Presupuestos.
 *
 * RF15: Definir presupuesto mensual por categoría.
 * RF16: Calcular automáticamente el porcentaje de uso.
 * RF17: Mostrar alertas cuando se supere el umbral configurable.
 * RF18: Modificar presupuestos.
 */
@Controller
@RequestMapping("/presupuestos")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    // ── Listar presupuestos activos ───────────────────────────────────────

    @GetMapping
    public String listar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long cuentaId,
            Model model) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());
        var cuentas = userService.listarCuentas(usuario);

        model.addAttribute("cuentas", cuentas);
        model.addAttribute("categorias", categoryRepository.findAll());
        model.addAttribute("presupuestoDto", new PresupuestoDto());

        if (cuentaId != null) {
            var presupuestos = budgetService.listarActivos(cuentaId, usuario);
            // Calcular porcentaje de uso para cada presupuesto
            var presupuestosConUso = presupuestos.stream()
                    .map(b -> {
                        double pct = budgetService.calcularPorcentajeUso(b);
                        return new Object[]{b, pct};
                    }).toList();

            model.addAttribute("presupuestosConUso", presupuestosConUso);
            model.addAttribute("cuentaSeleccionada", cuentaId);
        }

        return "budget/lista";
    }

    // ── Crear presupuesto ─────────────────────────────────────────────────

    @PostMapping("/nuevo")
    public String crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute PresupuestoDto dto,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            budgetService.crear(dto, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Presupuesto creado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/presupuestos?cuentaId=" + dto.getAccountId();
    }

    // ── Actualizar presupuesto ────────────────────────────────────────────

    @PostMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute PresupuestoDto dto,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            budgetService.actualizar(id, dto, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Presupuesto actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/presupuestos?cuentaId=" + dto.getAccountId();
    }

    // ── Eliminar presupuesto ──────────────────────────────────────────────

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            @RequestParam Long cuentaId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            budgetService.eliminar(id, cuentaId, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Presupuesto eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/presupuestos?cuentaId=" + cuentaId;
    }
}
