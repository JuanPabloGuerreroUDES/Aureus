package com.aureus.repository;

import com.aureus.model.Mensaje;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /** Bandeja de entrada: mensajes donde el receptor es el usuario actual. */
    List<Mensaje> findByReceptorOrderByEnviadoEnDesc(User receptor);

    /** Mensajes enviados por el usuario actual. */
    List<Mensaje> findByEmisorOrderByEnviadoEnDesc(User emisor);

    /** Cuenta mensajes no leídos del receptor. */
    long countByReceptorAndLeidoFalse(User receptor);
}
