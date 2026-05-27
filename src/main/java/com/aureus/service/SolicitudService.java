package com.aureus.service;

import com.aureus.dto.solicitud.SolicitudEntradaDto;
import com.aureus.dto.solicitud.SolicitudSalidaDto;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    SolicitudSalidaDto        abrir(String emailCreador, SolicitudEntradaDto dto);

    List<SolicitudSalidaDto>  propias(String emailCreador);

    List<SolicitudSalidaDto>  listado();

    SolicitudSalidaDto        resolver(Long id, String nota, boolean aprobada);

    Map<String, Long>         resumen();
}
