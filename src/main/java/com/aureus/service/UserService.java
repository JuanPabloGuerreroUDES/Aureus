package com.aureus.service;

import com.aureus.dto.auth.CambiarPasswordDto;
import com.aureus.dto.auth.RegistroDto;
import com.aureus.dto.user.ActualizarPerfilDto;
import com.aureus.model.User;

/**
 * Contrato del servicio de usuarios.
 *
 * Los controladores dependen de esta interfaz, no de la implementación concreta
 * (Dependency Inversion Principle — U11 §2.5).
 * Esto permite:
 *   - Cambiar la implementación sin modificar los controladores.
 *   - Crear implementaciones mock en pruebas (@MockBean del contrato).
 */
public interface UserService {
    User registrar(RegistroDto dto);
    User buscarPorEmail(String email);
    User buscarPorId(Long id);
    User actualizarPerfil(Long userId, ActualizarPerfilDto dto);
    void cambiarPassword(Long userId, CambiarPasswordDto dto);
    void desactivarCuenta(Long userId);
}
