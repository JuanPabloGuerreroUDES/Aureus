package com.aureus.dto.mensaje;

import com.aureus.model.Mensaje;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de salida para representar un mensaje en las respuestas de la API (Módulo 1).
 * No expone las entidades User completas — solo los campos necesarios.
 */
@Getter
public class MensajeDto {

    private final Long id;
    private final String emisorEmail;
    private final String emisorNombre;
    private final String receptorEmail;
    private final String asunto;
    private final String contenido;
    private final boolean leido;
    private final LocalDateTime enviadoEn;

    public MensajeDto(Mensaje m) {
        this.id            = m.getId();
        this.emisorEmail   = m.getEmisor().getEmail();
        this.emisorNombre  = m.getEmisor().getName();
        this.receptorEmail = m.getReceptor().getEmail();
        this.asunto        = m.getAsunto();
        this.contenido     = m.getContenido();
        this.leido         = m.isLeido();
        this.enviadoEn     = m.getEnviadoEn();
    }
}
