package com.aureus.controller;

import com.aureus.dto.solicitud.CrearSolicitudDto;
import com.aureus.dto.solicitud.SolicitudDto;
import com.aureus.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el sistema de solicitudes con flujo de estados (Módulo 2 — Examen Final).
 */
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    /**
     * POST /api/solicitudes
     * Radica una nueva solicitud con estado PENDIENTE.
     * Retorna 201 Created.
     */
    @PostMapping
    public ResponseEntity<SolicitudDto> crear(
            @Valid @RequestBody CrearSolicitudDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        SolicitudDto creada = solicitudService.crear(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * GET /api/solicitudes/mis-solicitudes
     * Lista únicamente las solicitudes del usuario autenticado.
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<SolicitudDto>> misSolicitudes(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(solicitudService.misSolicitudes(userDetails.getUsername()));
    }

    /**
     * GET /api/solicitudes
     * Lista todas las solicitudes del sistema. Solo ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudDto>> todas() {
        return ResponseEntity.ok(solicitudService.todas());
    }

    /**
     * PUT /api/solicitudes/{id}/aprobar?observacion=texto
     * Aprueba una solicitud. Solo ADMIN.
     * Retorna 200 con la solicitud actualizada.
     */
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudDto> aprobar(
            @PathVariable Long id,
            @RequestParam String observacion) {

        return ResponseEntity.ok(solicitudService.aprobar(id, observacion));
    }

    /**
     * PUT /api/solicitudes/{id}/rechazar?observacion=texto
     * Rechaza una solicitud. Solo ADMIN.
     * Retorna 200 con la solicitud actualizada.
     */
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudDto> rechazar(
            @PathVariable Long id,
            @RequestParam String observacion) {

        return ResponseEntity.ok(solicitudService.rechazar(id, observacion));
    }
}
