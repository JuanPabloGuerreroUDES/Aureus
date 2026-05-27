package com.aureus.repository;

import com.aureus.model.Solicitud;
import com.aureus.model.Solicitud.EstadoSolicitud;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Solicitudes propias del usuario. */
    List<Solicitud> findBySolicitanteOrderByFechaCreacionDesc(User solicitante);

    long countByEstado(EstadoSolicitud estado);
}
