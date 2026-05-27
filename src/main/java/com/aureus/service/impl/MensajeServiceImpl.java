package com.aureus.service.impl;

import com.aureus.dto.mensaje.EnviarMensajeDto;
import com.aureus.dto.mensaje.MensajeDto;
import com.aureus.exception.AccesoDenegadoException;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.model.Mensaje;
import com.aureus.model.User;
import com.aureus.repository.MensajeRepository;
import com.aureus.repository.UserRepository;
import com.aureus.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UserRepository    userRepository;

    @Override
    @Transactional
    public MensajeDto enviar(String emailEmisor, EnviarMensajeDto dto) {
        User emisor = findUser(emailEmisor);
        User receptor = userRepository.findByEmail(dto.getEmailReceptor())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario receptor no encontrado: " + dto.getEmailReceptor()));

        Mensaje m = new Mensaje();
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setAsunto(dto.getAsunto());
        m.setContenido(dto.getContenido());

        return new MensajeDto(mensajeRepository.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDto> bandeja(String emailReceptor) {
        User receptor = findUser(emailReceptor);
        return mensajeRepository.findByReceptorOrderByEnviadoEnDesc(receptor)
                .stream().map(MensajeDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeDto> enviados(String emailEmisor) {
        User emisor = findUser(emailEmisor);
        return mensajeRepository.findByEmisorOrderByEnviadoEnDesc(emisor)
                .stream().map(MensajeDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void marcarLeido(Long mensajeId, String emailReceptor) {
        Mensaje m = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje", mensajeId));

        // Solo el receptor puede marcar como leído (IDOR prevention)
        if (!m.getReceptor().getEmail().equals(emailReceptor)) {
            throw new AccesoDenegadoException();
        }
        m.setLeido(true);
        mensajeRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countNoLeidos(String emailReceptor) {
        User receptor = findUser(emailReceptor);
        long count = mensajeRepository.countByReceptorAndLeidoFalse(receptor);
        return Map.of("count", count);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
