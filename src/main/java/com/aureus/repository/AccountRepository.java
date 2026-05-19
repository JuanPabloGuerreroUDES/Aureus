package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Cuenta.
 *
 * IDOR prevention: todos los métodos filtran por 'user' para garantizar
 * que un usuario solo acceda a sus propias cuentas.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /** Todas las cuentas del usuario autenticado. */
    List<Account> findByUser(User user);

    /** Busca una cuenta por ID y usuario (previene IDOR). */
    Optional<Account> findByIdAndUser(Long id, User user);

    /** Verifica si el usuario ya tiene una cuenta con ese nombre. */
    boolean existsByNameAndUser(String name, User user);
}
