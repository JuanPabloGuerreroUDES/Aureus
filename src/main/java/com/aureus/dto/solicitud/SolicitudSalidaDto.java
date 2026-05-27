package com.aureus.dto.solicitud;

import com.aureus.model.Solicitud;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SolicitudSalidaDto {

    private final Long          id;
    private final String        creadorEmail;
    private final String        creadorNombre;
    private final String        tipo;
    private final String        contenido;
    private final String        estado;
    private final String        nota;
    private final LocalDateTime abiertaEn;
    private final LocalDateTime cerradaEn;

    public SolicitudSalidaDto(Solicitud s) {
        this.id            = s.getId();
        this.creadorEmail  = s.getCreador().getEmail();
        this.creadorNombre = s.getCreador().getName();
        this.tipo          = s.getTipo().name();
        this.contenido     = s.getContenido();
        this.estado        = s.getEstado().name();
        this.nota          = s.getNota();
        this.abiertaEn     = s.getAbiertaEn();
        this.cerradaEn     = s.getCerradaEn();
    }
}
