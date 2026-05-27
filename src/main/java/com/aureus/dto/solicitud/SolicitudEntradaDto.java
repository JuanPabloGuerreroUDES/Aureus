package com.aureus.dto.solicitud;

import com.aureus.model.Solicitud.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SolicitudEntradaDto {

    @NotNull(message = "El tipo es obligatorio: SOPORTE, ACCESO, INFORMACION")
    private TipoSolicitud tipo;

    @NotBlank(message = "El contenido de la solicitud no puede estar vacío")
    private String contenido;
}
