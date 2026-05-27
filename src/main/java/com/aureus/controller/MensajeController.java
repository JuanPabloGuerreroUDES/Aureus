package com.aureus.controller;

import com.aureus.dto.mensaje.MensajeEntradaDto;
import com.aureus.dto.mensaje.MensajeSalidaDto;
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

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService svc;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeSalidaDto enviar(
            @Valid @RequestBody MensajeEntradaDto dto,
            @AuthenticationPrincipal UserDetails ud) {

        return svc.crear(ud.getUsername(), dto);
    }

    @GetMapping("/bandeja-entrada")
    public List<MensajeSalidaDto> bandeja(
            @AuthenticationPrincipal UserDetails ud) {

        return svc.recibidos(ud.getUsername());
    }

    @GetMapping("/enviados")
    public List<MensajeSalidaDto> enviados(
            @AuthenticationPrincipal UserDetails ud) {

        return svc.emitidos(ud.getUsername());
    }

    @PutMapping("/{id}/leer")
    @ResponseStatus(HttpStatus.OK)
    public void leer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {

        svc.marcarVisto(id, ud.getUsername());
    }

    @GetMapping("/no-leidos/count")
    public Map<String, Long> sinLeer(
            @AuthenticationPrincipal UserDetails ud) {

        return svc.sinLeer(ud.getUsername());
    }
}
