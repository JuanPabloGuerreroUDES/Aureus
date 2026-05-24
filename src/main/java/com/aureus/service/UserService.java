package com.aureus.service;

import com.aureus.dto.auth.CambiarPasswordDto;
import com.aureus.dto.auth.RegistroDto;
import com.aureus.dto.user.ActualizarPerfilDto;
import com.aureus.model.Account;
import com.aureus.model.User;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Contrato del servicio de usuarios.
 *
 * Las anotaciones @PreAuthorize en la interfaz se propagan a todas las
 * implementaciones, garantizando que la autorización se aplique siempre,
 * sin importar qué implementación use el controlador (DIP + U9 §5.2).
 *
 * Reglas de acceso:
 *   - registrar, buscarPorEmail, buscarPorId → sin restricción (llamados por el sistema)
 *   - actualizarPerfil, cambiarPassword     → ADMIN o el propio usuario
 *   - desactivarCuenta                       → solo ADMIN
 *   - listarCuentas                          → cualquier usuario autenticado
 */
public interface UserService {

    /** Registra un nuevo usuario. Público — usado en /auth/registro. */
    User registrar(RegistroDto dto);

    /** Busca por email. Llamado internamente por Spring Security y controladores. */
    User buscarPorEmail(String email);

    /** Busca por ID. Sin restricción de acceso (la verificación es responsabilidad del caller). */
    User buscarPorId(Long id);

    /**
     * Actualiza el nombre del usuario.
     *
     * @PreAuthorize: ADMIN puede editar cualquier perfil.
     * El propio usuario puede editar el suyo comparando el ID del path
     * con el ID del usuario autenticado mediante SpEL (U9 §5.2).
     */
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.buscarPorEmail(authentication.name).id == #userId")
    User actualizarPerfil(Long userId, ActualizarPerfilDto dto);

    /**
     * Cambia la contraseña — verifica la contraseña actual con BCrypt (U9 §4.1).
     *
     * @PreAuthorize: solo el propio usuario o ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.buscarPorEmail(authentication.name).id == #userId")
    void cambiarPassword(Long userId, CambiarPasswordDto dto);

    /**
     * Desactiva una cuenta (soft delete — campo 'activo' = false).
     *
     * @PreAuthorize: EXCLUSIVAMENTE ADMIN (U9 §5.2).
     * Un usuario no puede desactivar su propia cuenta desde la UI
     * para evitar bloqueos accidentales.
     */
    @PreAuthorize("hasRole('ADMIN')")
    void desactivarCuenta(Long userId);

    /**
     * Lista las cuentas financieras del usuario.
     * Evita acceso directo al repositorio desde controladores (arquitectura por capas).
     */
    List<Account> listarCuentas(User usuario);
}
