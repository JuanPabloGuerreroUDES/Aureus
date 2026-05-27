package com.aureus.service.impl;

import com.aureus.dto.mensaje.MensajeEntradaDto;
import com.aureus.dto.mensaje.MensajeSalidaDto;
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

    private final MensajeRepository repo;
    private final UserRepository    usuarios;

    @Override
    @Transactional
    public MensajeSalidaDto crear(String emailAutor, MensajeEntradaDto dto) {
        User autor   = resolverUsuario(emailAutor);
        User receptor = usuarios.findByEmail(dto.getPara())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Destinatario no registrado en el sistema"));

        Mensaje m = new Mensaje();
        m.setAutor(autor);
        m.setReceptor(receptor);
        m.setTitulo(dto.getTitulo());
        m.setTexto(dto.getTexto());

        return new MensajeSalidaDto(repo.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeSalidaDto> recibidos(String emailReceptor) {
        return repo.findByReceptorOrderByCreadoEnDesc(resolverUsuario(emailReceptor))
                .stream().map(MensajeSalidaDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeSalidaDto> emitidos(String emailAutor) {
        return repo.findByAutorOrderByCreadoEnDesc(resolverUsuario(emailAutor))
                .stream().map(MensajeSalidaDto::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void marcarVisto(Long id, String emailReceptor) {
        Mensaje m = repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje", id));

        if (!m.getReceptor().getEmail().equals(emailReceptor)) {
            throw new AccesoDenegadoException();
        }
        m.setVisto(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> sinLeer(String emailReceptor) {
        long n = repo.countByReceptorAndVistoFalse(resolverUsuario(emailReceptor));
        return Map.of("count", n);
    }

    private User resolverUsuario(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
