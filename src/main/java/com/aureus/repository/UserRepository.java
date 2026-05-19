package com.aureus.repository;

import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de Usuario.
 *
 * Spring Data JPA genera la implementación automáticamente en tiempo de ejecución.
 *
 * Seguridad (SQL Injection):
 *   Los métodos de Spring Data JPA usan PreparedStatements internamente.
 *   Nunca construir consultas concatenando strings del usuario.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por email (case-sensitive).
     * Usado por UserDetailsService para la autenticación.
     *
     * JPQL generada: SELECT u FROM User u WHERE u.email = :email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con ese email.
     * Usado en el registro para evitar duplicados.
     */
    boolean existsByEmail(String email);
}
