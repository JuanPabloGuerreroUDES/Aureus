package com.aureus.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador del panel de administración.
 * FIX: el sidebar apuntaba a /admin pero solo existía /admin/usuarios.
 * Esta clase crea el endpoint /admin que faltaba.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping
    public String panelAdmin() {
        return "admin/index";
    }
}
