package com.aureus.controller;

import com.aureus.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/solicitudes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SolicitudPanelController {

    private final SolicitudService svc;

    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("resumen",     svc.resumen());
        model.addAttribute("solicitudes", svc.listado());
        return "admin/solicitudes-panel";
    }
}
