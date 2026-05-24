package com.aureus.controller;

import com.aureus.dto.auth.CambiarPasswordDto;
import com.aureus.dto.user.ActualizarPerfilDto;
import com.aureus.model.User;
import com.aureus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Perfil y Administración de Usuarios.
 *
 * Demuestra autorización a nivel de método con @PreAuthorize (U9 §5.2).
 *
 * Reglas de acceso:
 *   - /perfil/**  → cualquier usuario autenticado puede ver/editar su propio perfil.
 *   - /admin/usuarios/** → solo ROLE_ADMIN (control granular por método).
 *
 * @PreAuthorize se evalúa ANTES de ejecutar el método.
 * Si falla, Spring Security lanza AccessDeniedException → GlobalExceptionHandler.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ── Perfil del usuario autenticado ────────────────────────────────────

    /**
     * Muestra el formulario de perfil.
     * Cualquier usuario autenticado accede a su propio perfil.
     */
    @GetMapping("/perfil")
    public String mostrarPerfil(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User usuario = userService.buscarPorEmail(userDetails.getUsername());
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilDto", new ActualizarPerfilDto());
        model.addAttribute("passwordDto", new CambiarPasswordDto());
        return "user/perfil";
    }

    /**
     * Actualiza el nombre del usuario autenticado.
     *
     * @PreAuthorize: el usuario solo puede editar su propio perfil.
     * La expresión SpEL compara el ID del path con el ID del usuario autenticado
     * O permite acceso si es ADMIN (U9 §5.2).
     */
    @PostMapping("/perfil/{id}/actualizar")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.buscarPorEmail(authentication.name).id == #id")
    public String actualizarPerfil(@PathVariable Long id,
                                   @Valid @ModelAttribute("perfilDto") ActualizarPerfilDto dto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Datos inválidos");
            return "redirect:/perfil";
        }
        userService.actualizarPerfil(id, dto);
        redirectAttributes.addFlashAttribute("successMsg", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Verifica la contraseña actual antes de cambiarla (U9 §4.1).
     */
    @PostMapping("/perfil/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.buscarPorEmail(authentication.name).id == #id")
    public String cambiarPassword(@PathVariable Long id,
                                  @Valid @ModelAttribute("passwordDto") CambiarPasswordDto dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Datos inválidos");
            return "redirect:/perfil";
        }
        try {
            userService.cambiarPassword(id, dto);
            redirectAttributes.addFlashAttribute("successMsg", "Contraseña actualizada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/perfil";
    }

    // ── Panel de administración (solo ROLE_ADMIN) ─────────────────────────

    /**
     * Lista todos los usuarios — solo ADMIN.
     *
     * @PreAuthorize("hasRole('ADMIN')") → evaluado ANTES de ejecutar el método.
     * Si el usuario no es ADMIN, AccessDeniedException → GlobalExceptionHandler → /error
     */
    @GetMapping("/admin/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        // En una implementación real usaría un UserRepository.findAll()
        // Para demostrar @PreAuthorize, solo muestra la vista.
        return "admin/usuarios";
    }

    /**
     * Desactiva la cuenta de un usuario — solo ADMIN.
     * Equivale a un "soft delete" (U9 §4.2 — campo 'activo').
     *
     * Nota de seguridad: la cuenta desactivada no puede autenticarse
     * porque AureusUserDetailsService llama .disabled(!user.isActivo()).
     */
    @PostMapping("/admin/usuarios/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivarUsuario(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.desactivarCuenta(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cuenta id=" + id + " desactivada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
