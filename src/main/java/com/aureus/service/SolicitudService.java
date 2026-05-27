package com.aureus.service;

import com.aureus.model.Solicitud;
import com.aureus.model.User;
import com.aureus.model.enums.EstadoSolicitud;
import com.aureus.model.enums.TipoSolicitud;
import com.aureus.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepo;

    public Solicitud crear(TipoSolicitud tipo, String descripcion, User solicitante) {
        Solicitud s = new Solicitud();
        s.setTipo(tipo);
        s.setDescripcion(descripcion);
        s.setSolicitante(solicitante);
        return solicitudRepo.save(s);
    }

    public List<Solicitud> misSolicitudes(User solicitante) {
        return solicitudRepo.findBySolicitante(solicitante);
    }

    public List<Solicitud> todas() {
        return solicitudRepo.findAll();
    }

    public Solicitud aprobar(Long id, String observacion) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setEstado(EstadoSolicitud.APROBADA);
        s.setObservacion(observacion);
        s.setFechaResolucion(LocalDateTime.now());
        return solicitudRepo.save(s);
    }

    public Solicitud rechazar(Long id, String observacion) {
        Solicitud s = solicitudRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setEstado(EstadoSolicitud.RECHAZADA);
        s.setObservacion(observacion);
        s.setFechaResolucion(LocalDateTime.now());
        return solicitudRepo.save(s);
    }
}
