package com.aureus.service.impl;

import com.aureus.dto.auth.CambiarPasswordDto;
import com.aureus.dto.auth.RegistroDto;
import com.aureus.dto.user.ActualizarPerfilDto;
import com.aureus.exception.EmailDuplicadoException;
import com.aureus.exception.PasswordIncorrectoException;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.exception.ValidacionException;
import com.aureus.model.Account;
import com.aureus.model.Account;
import com.aureus.model.User;
import java.util.List;
import com.aureus.repository.AccountRepository;
import com.aureus.repository.UserRepository;
import com.aureus.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de usuarios.
 *
 * CAMBIOS vs versión anterior (U11 - SOLID + Clean Code):
 *  - Implementa la interfaz UserService (DIP §2.5).
 *  - Cada método hace UNA sola cosa (SRP §2.1, Clean Code §3.2).
 *  - Logging con SLF4J usando placeholders, no concatenación (U11 §6.2).
 *  - Usa las nuevas excepciones individuales en lugar de Dtos.AureusExceptions (ISP §2.4).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // U8 §7.2: lecturas no necesitan transacción de escritura
public class UserServiceImpl implements UserService {

    private final UserRepository    userRepository;
    private final AccountRepository  accountRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    @Transactional
    public User registrar(RegistroDto dto) {
        log.info("Registrando nuevo usuario: {}", dto.getEmail());
        validarRegistro(dto);

        User user = construirUsuario(dto);
        User guardado = userRepository.save(user);
        // Crear cuenta principal por defecto para que pueda registrar transacciones de inmediato
        Account cuentaPrincipal = new Account("Cuenta principal", guardado);
        cuentaPrincipal.setEsPrincipal(true);
        accountRepository.save(cuentaPrincipal);
        log.info("Usuario registrado con id={} y cuenta principal creada", guardado.getId());
        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public User buscarPorEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con email " + email + " no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    @Override
    @Transactional
    public User actualizarPerfil(Long userId, ActualizarPerfilDto dto) {
        User user = buscarPorId(userId);
        user.setName(dto.getName());
        log.info("Perfil actualizado para usuario id={}", userId);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long userId, CambiarPasswordDto dto) {
        validarCambioPassword(dto);
        User user = buscarPorId(userId);
        verificarPasswordActual(dto.getPasswordActual(), user.getPassword());
        user.setPassword(passwordEncoder.encode(dto.getPasswordNuevo()));
        userRepository.save(user);
        log.info("Contraseña cambiada para usuario id={}", userId);
    }

    @Override
    @Transactional
    public void desactivarCuenta(Long userId) {
        User user = buscarPorId(userId);
        user.setActivo(false);
        userRepository.save(user);
        log.warn("Cuenta desactivada para usuario id={}", userId);
    }

    // ── Métodos privados auxiliares (Clean Code §3.2) ─────────────────────

    private void validarRegistro(RegistroDto dto) {
        if (!dto.passwordsCoinciden()) {
            throw new ValidacionException("Las contraseñas no coinciden");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }
    }

    private User construirUsuario(RegistroDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRol("ROLE_USER");
        user.setActivo(true);
        return user;
    }

    private void validarCambioPassword(CambiarPasswordDto dto) {
        if (!dto.passwordsCoinciden()) {
            throw new ValidacionException("Las contraseñas nuevas no coinciden");
        }
    }

    private void verificarPasswordActual(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new PasswordIncorrectoException();
        }
    }


    @Override
    public List<Account> listarCuentas(User usuario) {
        return accountRepository.findByUserOrderByEsPrincipalDescNameAsc(usuario);
    }
}
