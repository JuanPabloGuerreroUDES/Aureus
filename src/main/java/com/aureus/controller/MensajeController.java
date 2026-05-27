package com.aureus.controller;

import com.aureus.dto.mensaje.EnviarMensajeDto;
import com.aureus.dto.mensaje.MensajeDto;
import com.aureus.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el sistema de mensajería interna (Módulo 1 — Examen Final).
 *
 * Todos los endpoints requieren autenticación (configurado en SecurityConfig /api/**).
 */
@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    /**
     * POST /api/mensajes
     * Envía un mensaje a otro usuario del sistema.
     * Retorna 201 Created con el mensaje creado.
     */
    @PostMapping
    public ResponseEntity<MensajeDto> enviar(
            @Valid @RequestBody EnviarMensajeDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        MensajeDto creado = mensajeService.enviar(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * GET /api/mensajes/bandeja-entrada
     * Lista los mensajes recibidos por el usuario autenticado.
     */
    @GetMapping("/bandeja-entrada")
    public ResponseEntity<List<MensajeDto>> bandeja(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(mensajeService.bandeja(userDetails.getUsername()));
    }

    /**
     * GET /api/mensajes/enviados
     * Lista los mensajes enviados por el usuario autenticado.
     */
    @GetMapping("/enviados")
    public ResponseEntity<List<MensajeDto>> enviados(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(mensajeService.enviados(userDetails.getUsername()));
    }

    /**
     * PUT /api/mensajes/{id}/leer
     * Marca un mensaje recibido como leído.
     * Retorna 200 si existe, 404 si no existe, 403 si no es el receptor.
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        mensajeService.marcarLeido(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/mensajes/no-leidos/count
     * Retorna el número de mensajes no leídos: {"count": N}
     */
    @GetMapping("/no-leidos/count")
    public ResponseEntity<Map<String, Long>> countNoLeidos(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(mensajeService.countNoLeidos(userDetails.getUsername()));
    }
}
