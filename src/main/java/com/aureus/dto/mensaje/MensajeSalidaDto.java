package com.aureus.dto.mensaje;

import com.aureus.model.Mensaje;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MensajeSalidaDto {

    private final Long          id;
    private final String        de;
    private final String        para;
    private final String        titulo;
    private final String        texto;
    private final boolean       visto;
    private final LocalDateTime creadoEn;

    public MensajeSalidaDto(Mensaje m) {
        this.id       = m.getId();
        this.de       = m.getAutor().getEmail();
        this.para     = m.getReceptor().getEmail();
        this.titulo   = m.getTitulo();
        this.texto    = m.getTexto();
        this.visto    = m.isVisto();
        this.creadoEn = m.getCreadoEn();
    }
}
