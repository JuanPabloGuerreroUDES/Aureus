package com.aureus.controller;

import com.aureus.dto.goal.MetaAhorroDto;
import com.aureus.dto.goal.AporteMetaDto;
import com.aureus.model.User;
import com.aureus.service.SavingsGoalService;
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

/**
 * Controlador de Metas de Ahorro.
 *
 * RF19: Crear metas con monto objetivo y fecha límite.
 * RF20: Calcular el progreso de cada meta.
 * RF21: Registrar aportes a una meta.
 * RF22: Editar y eliminar metas.
 */
@Controller
@RequestMapping("/metas")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService goalService;
    private final UserService userService;

    // ── Listar ───────────────────────────────────────────────────────────

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User usuario = userService.buscarPorEmail(userDetails.getUsername());
        model.addAttribute("metas", goalService.listarPorUsuario(usuario));
        model.addAttribute("metaDto", new MetaAhorroDto());
        model.addAttribute("aporteDto", new AporteMetaDto());
        return "goal/lista";
    }

    // ── RF19: Crear meta ──────────────────────────────────────────────────

    @PostMapping("/nueva")
    public String crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("metaDto") MetaAhorroDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Datos de la meta inválidos");
            return "redirect:/metas";
        }

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            goalService.crear(dto, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Meta creada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/metas";
    }

    // ── RF21: Registrar aporte ────────────────────────────────────────────

    @PostMapping("/aporte")
    public String registrarAporte(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute AporteMetaDto dto,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            goalService.registrarAporte(dto, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Aporte registrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/metas";
    }

    // ── RF22: Editar meta ─────────────────────────────────────────────────

    @PostMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute MetaAhorroDto dto,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            goalService.editar(id, dto, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Meta actualizada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/metas";
    }

    // ── RF22: Eliminar meta ───────────────────────────────────────────────

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User usuario = userService.buscarPorEmail(userDetails.getUsername());

        try {
            goalService.eliminar(id, usuario);
            redirectAttributes.addFlashAttribute("successMsg", "Meta eliminada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/metas";
    }
}
