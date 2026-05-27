package com.aureus.service;

import com.aureus.dto.solicitud.CrearSolicitudDto;
import com.aureus.dto.solicitud.SolicitudDto;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    /** Radica una nueva solicitud con estado PENDIENTE. */
    SolicitudDto crear(String emailSolicitante, CrearSolicitudDto dto);

    /** Retorna solo las solicitudes del usuario autenticado. */
    List<SolicitudDto> misSolicitudes(String emailSolicitante);

    /** Retorna todas las solicitudes del sistema (ADMIN). */
    List<SolicitudDto> todas();

    /** Aprueba la solicitud y registra la observación (ADMIN). */
    SolicitudDto aprobar(Long id, String observacion);

    /** Rechaza la solicitud y registra la observación (ADMIN). */
    SolicitudDto rechazar(Long id, String observacion);

    /** KPIs para el panel visual: total, pendientes, aprobadas, rechazadas. */
    Map<String, Long> kpis();
}
