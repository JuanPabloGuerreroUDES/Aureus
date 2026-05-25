package com.aureus.controller;

import com.aureus.dto.budget.PresupuestoDto;
import com.aureus.model.Budget;
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

import java.util.List;

/**
 * Controlador de Presupuestos.
 *
 * BUG CORREGIDO: el modelo pasaba 'presupuestosConUso' pero el JSP iteraba
 * 'presupuestos'. Ahora se pasan ambos: 'presupuestos' (List<Budget>) y
 * 'porcentajesUso' (List<Double>) en el mismo orden para poder iterar
 * combinados en el JSP mediante índice o un DTO.
 */
@Controller
@RequestMapping("/presupuestos")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService      budgetService;
    private final UserService        userService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String listar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long cuentaId,
            Model model) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());
        var cuentas  = userService.listarCuentas(usuario);

        model.addAttribute("cuentas",       cuentas);
        model.addAttribute("categorias",    categoryRepository.findAll());
        model.addAttribute("presupuestoDto", new PresupuestoDto());
        model.addAttribute("cuentaSeleccionada", cuentaId);

        if (cuentaId != null) {
            List<Budget> presupuestos = budgetService.listarActivos(cuentaId, usuario);

            // FIX: nombre correcto que usa el JSP + porcentajes en el mismo orden
            model.addAttribute("presupuestos", presupuestos);
            model.addAttribute("porcentajesUso",
                presupuestos.stream()
                    .map(budgetService::calcularPorcentajeUso)
                    .toList());
        }

        return "budget/lista";
    }

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
