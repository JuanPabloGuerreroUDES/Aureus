package com.aureus.controller;

import com.aureus.dto.auth.JwtResponseDto;
import com.aureus.dto.auth.RegistroDto;
import com.aureus.exception.EmailDuplicadoException;
import com.aureus.model.User;
import com.aureus.security.JwtUtils;
import com.aureus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticación JWT.
 *
 * Implementa el flujo de autenticación stateless (U9 §7.2):
 *   POST /api/auth/login   → verifica credenciales → devuelve JWT
 *   POST /api/auth/registro → crea cuenta → devuelve JWT directo (auto-login)
 *
 * Este controlador es para clientes REST (móvil, SPA, Postman).
 * La autenticación web MVC sigue usando sesión + /auth/login (JSP).
 *
 * CSRF desactivado para /api/** porque:
 *   - El JWT viaja en el header Authorization, no en cookie.
 *   - Los ataques CSRF se basan en el envío automático de cookies (U9 §6.1).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils              jwtUtils;
    private final UserService           userService;

    // ── POST /api/auth/login ──────────────────────────────────────────────

    /**
     * Paso 1-3 del flujo JWT (U9 §7.2):
     * Verifica credenciales con BCrypt y devuelve un JWT firmado.
     *
     * Body JSON esperado: { "email": "...", "password": "..." }
     *
     * Respuesta 200: { "token": "eyJ...", "email": "...", "rol": "ROLE_USER", "tipo": "Bearer" }
     * Respuesta 401: credenciales incorrectas (mensaje genérico — A07 OWASP)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email    = credenciales.get("email");
        String password = credenciales.get("password");

        try {
            // AuthenticationManager delega a DaoAuthenticationProvider → BCrypt verify
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User usuario = userService.buscarPorEmail(userDetails.getUsername());

            // Genera JWT firmado (U9 §7.2 Paso 3)
            String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol());
            log.info("JWT generado para usuario: {}", email);

            return ResponseEntity.ok(new JwtResponseDto(token, usuario.getEmail(), usuario.getRol()));

        } catch (DisabledException e) {
            // Cuenta desactivada — no revelar si el email existe (A07 OWASP)
            log.warn("Intento de login con cuenta desactivada: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas o cuenta inactiva"));

        } catch (BadCredentialsException e) {
            // Contraseña incorrecta — mensaje genérico para no confirmar si el email existe
            log.warn("Credenciales incorrectas para: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas o cuenta inactiva"));
        }
    }

    // ── POST /api/auth/registro ───────────────────────────────────────────

    /**
     * Registra un nuevo usuario y devuelve un JWT directo.
     * El cliente queda autenticado sin pasar por /api/auth/login.
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroDto dto) {
        try {
            User usuario = userService.registrar(dto);
            String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol());
            log.info("Usuario registrado vía API: {}", usuario.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new JwtResponseDto(token, usuario.getEmail(), usuario.getRol()));

        } catch (EmailDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
