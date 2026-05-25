package com.aureus.controller;

import com.aureus.dto.account.CuentaDto;
import com.aureus.model.Account;
import com.aureus.model.User;
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

import java.util.List;

@Controller
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService     accountService;
    private final UserService        userService;
    private final TransactionService transactionService;

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails ud, Model model) {
        User usuario       = userService.buscarPorEmail(ud.getUsername());
        List<Account> list = accountService.listar(usuario);

        // Balance acumulado de cada cuenta (para las tarjetas)
        model.addAttribute("cuentas",     list);
        model.addAttribute("cuentaDto",   new CuentaDto());
        model.addAttribute("resumenes",
            list.stream()
                .map(c -> transactionService.calcularResumenCompleto(c.getId(), usuario))
                .toList());

        return "account/lista";
    }

    @PostMapping("/nueva")
    public String crear(@AuthenticationPrincipal UserDetails ud,
                        @Valid @ModelAttribute CuentaDto dto, BindingResult result,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg",
                result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).findFirst().orElse("Datos inválidos"));
            return "redirect:/cuentas";
        }
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            accountService.crear(dto, usuario);
            ra.addFlashAttribute("successMsg", "Cuenta '" + dto.getName() + "' creada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud,
                         @Valid @ModelAttribute CuentaDto dto, BindingResult result,
                         RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            accountService.editar(id, dto, usuario);
            ra.addFlashAttribute("successMsg", "Cuenta actualizada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/principal")
    public String marcarPrincipal(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud,
                                  RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            accountService.marcarComoPrincipal(id, usuario);
            ra.addFlashAttribute("successMsg", "Cuenta establecida como principal");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud,
                           RedirectAttributes ra) {
        User usuario = userService.buscarPorEmail(ud.getUsername());
        try {
            accountService.eliminar(id, usuario);
            ra.addFlashAttribute("successMsg", "Cuenta eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cuentas";
    }
}
