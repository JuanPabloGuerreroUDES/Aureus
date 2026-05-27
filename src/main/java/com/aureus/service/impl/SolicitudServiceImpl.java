package com.aureus.service.impl;

import com.aureus.dto.solicitud.CrearSolicitudDto;
import com.aureus.dto.solicitud.SolicitudDto;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.model.Solicitud;
import com.aureus.model.Solicitud.EstadoSolicitud;
import com.aureus.model.User;
import com.aureus.repository.SolicitudRepository;
import com.aureus.repository.UserRepository;
import com.aureus.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UserRepository      userRepository;

    @Override
    @Transactional
    public SolicitudDto crear(String emailSolicitante, CrearSolicitudDto dto) {
        User solicitante = findUser(emailSolicitante);

        Solicitud s = new Solicitud();
        s.setSolicitante(solicitante);
        s.setTipo(dto.getTipo());
        s.setDescripcion(dto.getDescripcion());
        // estado = PENDIENTE se asigna en @PrePersist

        return new SolicitudDto(solicitudRepository.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDto> misSolicitudes(String emailSolicitante) {
        User solicitante = findUser(emailSolicitante);
        return solicitudRepository.findBySolicitanteOrderByFechaCreacionDesc(solicitante)
                .stream().map(SolicitudDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDto> todas() {
        return solicitudRepository.findAll()
                .stream().map(SolicitudDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SolicitudDto aprobar(Long id, String observacion) {
        Solicitud s = findSolicitud(id);
        s.setEstado(EstadoSolicitud.APROBADA);
        s.setObservacion(observacion);
        s.setFechaResolucion(LocalDateTime.now());
        return new SolicitudDto(solicitudRepository.save(s));
    }

    @Override
    @Transactional
    public SolicitudDto rechazar(Long id, String observacion) {
        Solicitud s = findSolicitud(id);
        s.setEstado(EstadoSolicitud.RECHAZADA);
        s.setObservacion(observacion);
        s.setFechaResolucion(LocalDateTime.now());
        return new SolicitudDto(solicitudRepository.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> kpis() {
        long total      = solicitudRepository.count();
        long pendientes = solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE);
        long aprobadas  = solicitudRepository.countByEstado(EstadoSolicitud.APROBADA);
        long rechazadas = solicitudRepository.countByEstado(EstadoSolicitud.RECHAZADA);
        return Map.of(
                "total",      total,
                "pendientes", pendientes,
                "aprobadas",  aprobadas,
                "rechazadas", rechazadas
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Solicitud findSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", id));
    }
}
