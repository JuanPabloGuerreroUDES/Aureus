package com.aureus.dto.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensajeRequestDto {

    @NotNull(message = "El destinatario es obligatorio")
    @NotBlank(message = "El destinatario no puede estar vacío")
    private String destiUsername; // email del receptor

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;
}
