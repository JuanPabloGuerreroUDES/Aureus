package com.aureus.dto.solicitud;

import com.aureus.model.Solicitud.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para radicar una solicitud (Módulo 2).
 */
@Getter
@Setter
@NoArgsConstructor
public class CrearSolicitudDto {

    @NotNull(message = "El tipo de solicitud es obligatorio (SOPORTE, ACCESO, INFORMACION)")
    private TipoSolicitud tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
}
