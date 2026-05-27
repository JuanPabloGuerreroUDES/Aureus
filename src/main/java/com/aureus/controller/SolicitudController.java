package com.aureus.controller;

import com.aureus.dto.solicitud.SolicitudEntradaDto;
import com.aureus.dto.solicitud.SolicitudSalidaDto;
import com.aureus.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService svc;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudSalidaDto crear(
            @Valid @RequestBody SolicitudEntradaDto dto,
            @AuthenticationPrincipal UserDetails ud) {

        return svc.abrir(ud.getUsername(), dto);
    }

    @GetMapping("/mis-solicitudes")
    public List<SolicitudSalidaDto> mias(
            @AuthenticationPrincipal UserDetails ud) {

        return svc.propias(ud.getUsername());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SolicitudSalidaDto> todas() {
        return svc.listado();
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public SolicitudSalidaDto aprobar(
            @PathVariable Long id,
            @RequestParam String observacion) {

        return svc.resolver(id, observacion, true);
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public SolicitudSalidaDto rechazar(
            @PathVariable Long id,
            @RequestParam String observacion) {

        return svc.resolver(id, observacion, false);
    }
}
