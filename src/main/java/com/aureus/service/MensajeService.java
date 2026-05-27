package com.aureus.service;

import com.aureus.model.Mensaje;
import com.aureus.model.User;
import com.aureus.repository.MensajeRepository;
import com.aureus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepo;
    private final UserRepository userRepo;

    public Mensaje enviar(String receptorEmail, String asunto, String contenido, User emisor) {
        if (receptorEmail == null || asunto == null || contenido == null) {
            throw new IllegalArgumentException("Campos obligatorios faltantes");
        }

        User receptor = userRepo.findByEmail(receptorEmail)
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));

        Mensaje m = new Mensaje();
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setAsunto(asunto);
        m.setContenido(contenido);

        return mensajeRepo.save(m);
    }

    public List<Mensaje> bandejaEntrada(User receptor) {
        return mensajeRepo.findByReceptor(receptor);
    }

    public List<Mensaje> mensajesEnviados(User emisor) {
        return mensajeRepo.findByEmisor(emisor);
    }

    public Mensaje marcarLeido(Long id, User usuario) {
        Mensaje m = mensajeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        if (!m.getReceptor().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No autorizado");
        }

        m.setLeido(true);
        return mensajeRepo.save(m);
    }

    public long contarNoLeidos(User receptor) {
        return mensajeRepo.countByReceptorAndLeidoFalse(receptor);
    }
}
