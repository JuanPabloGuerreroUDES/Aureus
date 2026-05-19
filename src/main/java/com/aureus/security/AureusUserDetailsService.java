package com.aureus.security;

import com.aureus.model.User;
import com.aureus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de UserDetailsService para Spring Security.
 *
 * Flujo de autenticación (Unidad 9 - Sección 4.3):
 *   1. El usuario envía email + contraseña en el formulario de login.
 *   2. Spring Security llama a loadUserByUsername(email).
 *   3. Este método carga el usuario desde la BD.
 *   4. DaoAuthenticationProvider verifica la contraseña con BCrypt.
 *   5. Si es válida, crea el SecurityContext con el Authentication del usuario.
 *
 * Nota de seguridad:
 *   El mensaje de error en UsernameNotFoundException NO debe revelar si
 *   el usuario existe o no (enumeración de usuarios). El mensaje genérico
 *   "Credenciales inválidas" se configura en SecurityConfig.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AureusUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario de la BD por su email para que Spring Security
     * pueda verificar sus credenciales.
     *
     * @param email correo electrónico (usado como username)
     * @return UserDetails con email, hash de contraseña y rol
     * @throws UsernameNotFoundException si el email no existe en la BD
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Intentando autenticar usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de login con email no registrado: {}", email);
                    // El mensaje genérico evita revelar si el email existe (A07 OWASP)
                    return new UsernameNotFoundException("Credenciales inválidas");
                });

        log.debug("Usuario encontrado: {} | rol: {} | activo: {}", email, user.getRol(), user.isActivo());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                // La contraseña ya está hasheada con BCrypt en la BD
                .password(user.getPassword())
                // Rol con prefijo ROLE_ para que Spring Security lo reconozca
                .authorities(List.of(new SimpleGrantedAuthority(user.getRol())))
                // Cuenta desactivada si activo = false
                .disabled(!user.isActivo())
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(!user.isActivo())
                .build();
    }
}
