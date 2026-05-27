package com.aureus.dto.solicitud;

import com.aureus.model.Solicitud;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de salida para representar una solicitud en las respuestas de la API (Módulo 2).
 */
@Getter
public class SolicitudDto {

    private final Long id;
    private final String solicitanteEmail;
    private final String solicitanteNombre;
    private final String tipo;
    private final String descripcion;
    private final String estado;
    private final String observacion;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaResolucion;

    public SolicitudDto(Solicitud s) {
        this.id                  = s.getId();
        this.solicitanteEmail    = s.getSolicitante().getEmail();
        this.solicitanteNombre   = s.getSolicitante().getName();
        this.tipo                = s.getTipo().name();
        this.descripcion         = s.getDescripcion();
        this.estado              = s.getEstado().name();
        this.observacion         = s.getObservacion();
        this.fechaCreacion       = s.getFechaCreacion();
        this.fechaResolucion     = s.getFechaResolucion();
    }
}
