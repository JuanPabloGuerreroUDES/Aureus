package com.aureus.service;

import com.aureus.dto.mensaje.MensajeEntradaDto;
import com.aureus.dto.mensaje.MensajeSalidaDto;

import java.util.List;
import java.util.Map;

public interface MensajeService {

    MensajeSalidaDto        crear(String emailAutor, MensajeEntradaDto dto);

    List<MensajeSalidaDto>  recibidos(String emailReceptor);

    List<MensajeSalidaDto>  emitidos(String emailAutor);

    void                    marcarVisto(Long id, String emailReceptor);

    Map<String, Long>       sinLeer(String emailReceptor);
}
