package com.aureus.controller;

import com.aureus.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador MVC para el panel visual de solicitudes (Módulo 3 — Examen Final).
 *
 * Accesible en: GET /admin/solicitudes/panel
 * Solo usuarios con rol ADMIN pueden acceder (protegido en SecurityConfig y con @PreAuthorize).
 * El renderizado ocurre en el servidor usando Thymeleaf.
 */
@Controller
@RequestMapping("/admin/solicitudes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SolicitudPanelController {

    private final SolicitudService solicitudService;

    /**
     * GET /admin/solicitudes/panel
     * Renderiza la vista del panel con KPIs y la tabla de todas las solicitudes.
     */
    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("kpis",       solicitudService.kpis());
        model.addAttribute("solicitudes", solicitudService.todas());
        return "admin/solicitudes-panel";
    }
}
