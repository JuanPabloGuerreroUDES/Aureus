package com.aureus.service.impl;

import com.aureus.dto.goal.AporteMetaDto;
import com.aureus.dto.goal.MetaAhorroDto;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.model.SavingsGoal;
import com.aureus.model.User;
import com.aureus.repository.SavingsGoalRepository;
import com.aureus.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository goalRepository;

    @Override
    public SavingsGoal crear(MetaAhorroDto dto, User usuario) {
        SavingsGoal meta = new SavingsGoal(
                dto.getGoalName(), dto.getTargetAmount(),
                LocalDate.parse(dto.getDeadline()), usuario);
        log.info("Meta de ahorro creada: '{}' para usuario id={}", dto.getGoalName(), usuario.getId());
        return goalRepository.save(meta);
    }

    @Override
    public SavingsGoal registrarAporte(AporteMetaDto dto, User usuario) {
        SavingsGoal meta = obtenerMetaDelUsuario(dto.getGoalId(), usuario);
        meta.registrarAporte(dto.getAporte());
        log.info("Aporte de {} registrado en meta id={}", dto.getAporte(), dto.getGoalId());
        return goalRepository.save(meta);
    }

    @Override
    public SavingsGoal editar(Long id, MetaAhorroDto dto, User usuario) {
        SavingsGoal meta = obtenerMetaDelUsuario(id, usuario);
        meta.setGoalName(dto.getGoalName());
        meta.setTargetAmount(dto.getTargetAmount());
        meta.setDeadline(LocalDate.parse(dto.getDeadline()));
        return goalRepository.save(meta);
    }

    @Override
    public void eliminar(Long id, User usuario) {
        SavingsGoal meta = obtenerMetaDelUsuario(id, usuario);
        goalRepository.delete(meta);
        log.info("Meta de ahorro id={} eliminada", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoal> listarPorUsuario(User usuario) {
        return goalRepository.findByUser(usuario);
    }

    private SavingsGoal obtenerMetaDelUsuario(Long id, User usuario) {
        return goalRepository.findByIdAndUser(id, usuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Meta de ahorro", id));
    }
}
