package com.aureus.repository;

import com.aureus.model.Mensaje;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByReceptorOrderByCreadoEnDesc(User receptor);

    List<Mensaje> findByAutorOrderByCreadoEnDesc(User autor);

    long countByReceptorAndVistoFalse(User receptor);
}
