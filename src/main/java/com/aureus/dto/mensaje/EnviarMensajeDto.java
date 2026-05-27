package com.aureus.dto.mensaje;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para enviar un mensaje interno (Módulo 1).
 */
@Getter
@Setter
@NoArgsConstructor
public class EnviarMensajeDto {

    @NotBlank(message = "El email del destinatario es obligatorio")
    private String emailReceptor;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;
}
