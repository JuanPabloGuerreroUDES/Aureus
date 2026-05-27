package com.aureus.dto.mensaje;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MensajeEntradaDto {

    @NotBlank(message = "Debe indicar el destinatario")
    @Email(message = "El destinatario debe ser un email válido")
    private String para;

    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    @NotBlank(message = "El texto no puede estar vacío")
    private String texto;
}
