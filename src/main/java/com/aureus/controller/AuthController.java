package com.aureus.controller;

import com.aureus.dto.auth.RegistroDto;
import com.aureus.exception.EmailDuplicadoException;
import com.aureus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación.
 * Depende de la interfaz UserService (DIP), no de UserServiceImpl.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error  != null) model.addAttribute("errorMsg",  "Correo o contraseña incorrectos");
        if (logout != null) model.addAttribute("logoutMsg", "Sesión cerrada correctamente");
        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroDto", new RegistroDto());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("registroDto") RegistroDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) return "auth/registro";

        if (!dto.passwordsCoinciden()) {
            model.addAttribute("errorMsg", "Las contraseñas no coinciden");
            return "auth/registro";
        }

        try {
            userService.registrar(dto);
            redirectAttributes.addFlashAttribute("successMsg",
                    "¡Cuenta creada! Inicia sesión.");
            return "redirect:/auth/login";
        } catch (EmailDuplicadoException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/registro";
        }
    }
}
