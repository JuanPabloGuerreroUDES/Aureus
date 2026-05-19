package com.aureus.repository;

import com.aureus.model.SavingsGoal;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUser(User user);

    /** Protección IDOR: solo retorna la meta si pertenece al usuario. */
    Optional<SavingsGoal> findByIdAndUser(Long id, User user);

    // ELIMINADO: findByUserAndCheckCompletion — checkCompletion() es un método
    // calculado, no un campo persistido. Filtrar en la capa de servicio con Java.
}
