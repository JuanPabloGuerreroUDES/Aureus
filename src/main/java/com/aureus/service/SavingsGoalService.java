package com.aureus.service;

import com.aureus.dto.goal.AporteMetaDto;
import com.aureus.dto.goal.MetaAhorroDto;
import com.aureus.model.SavingsGoal;
import com.aureus.model.User;

import java.util.List;

public interface SavingsGoalService {
    SavingsGoal crear(MetaAhorroDto dto, User usuario);
    SavingsGoal registrarAporte(AporteMetaDto dto, User usuario);
    SavingsGoal editar(Long id, MetaAhorroDto dto, User usuario);
    void eliminar(Long id, User usuario);
    List<SavingsGoal> listarPorUsuario(User usuario);
}
