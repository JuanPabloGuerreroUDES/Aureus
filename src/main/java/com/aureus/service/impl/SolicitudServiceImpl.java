package com.aureus.service.impl;

import com.aureus.dto.solicitud.SolicitudEntradaDto;
import com.aureus.dto.solicitud.SolicitudSalidaDto;
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

    private final SolicitudRepository repo;
    private final UserRepository      usuarios;

    @Override
    @Transactional
    public SolicitudSalidaDto abrir(String emailCreador, SolicitudEntradaDto dto) {
        User creador = resolverUsuario(emailCreador);
        Solicitud s  = new Solicitud();
        s.setCreador(creador);
        s.setTipo(dto.getTipo());
        s.setContenido(dto.getContenido());
        return new SolicitudSalidaDto(repo.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudSalidaDto> propias(String emailCreador) {
        return repo.findByCreadorOrderByAbiertaEnDesc(resolverUsuario(emailCreador))
                .stream().map(SolicitudSalidaDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudSalidaDto> listado() {
        return repo.findAll().stream().map(SolicitudSalidaDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SolicitudSalidaDto resolver(Long id, String nota, boolean aprobada) {
        Solicitud s = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", id));
        s.setEstado(aprobada ? EstadoSolicitud.APROBADA : EstadoSolicitud.RECHAZADA);
        s.setNota(nota);
        s.setCerradaEn(LocalDateTime.now());
        return new SolicitudSalidaDto(repo.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> resumen() {
        return Map.of(
            "total",      repo.count(),
            "pendientes", repo.countByEstado(EstadoSolicitud.PENDIENTE),
            "aprobadas",  repo.countByEstado(EstadoSolicitud.APROBADA),
            "rechazadas", repo.countByEstado(EstadoSolicitud.RECHAZADA)
        );
    }

    private User resolverUsuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
