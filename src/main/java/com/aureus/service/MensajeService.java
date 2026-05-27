package com.aureus.service;

import com.aureus.dto.mensaje.EnviarMensajeDto;
import com.aureus.dto.mensaje.MensajeDto;

import java.util.List;
import java.util.Map;

public interface MensajeService {

    /** Envía un mensaje. El emisor es el usuario autenticado. */
    MensajeDto enviar(String emailEmisor, EnviarMensajeDto dto);

    /** Bandeja de entrada del usuario autenticado. */
    List<MensajeDto> bandeja(String emailReceptor);

    /** Mensajes enviados por el usuario autenticado. */
    List<MensajeDto> enviados(String emailEmisor);

    /** Marca un mensaje recibido como leído. Solo el receptor puede hacerlo. */
    void marcarLeido(Long mensajeId, String emailReceptor);

    /** Número de mensajes no leídos del usuario autenticado. */
    Map<String, Long> countNoLeidos(String emailReceptor);
}
