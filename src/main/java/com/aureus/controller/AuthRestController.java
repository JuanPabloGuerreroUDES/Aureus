package com.aureus.controller;

import com.aureus.dto.auth.JwtResponseDto;
import com.aureus.dto.auth.RegistroDto;
import com.aureus.dto.error.ApiError;
import com.aureus.exception.EmailDuplicadoException;
import com.aureus.model.User;
import com.aureus.security.JwtUtils;
import com.aureus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controlador REST para autenticación JWT — documentado con Swagger/OpenAPI (U11 §7.3).
 *
 * @Tag agrupa este controlador bajo "Autenticación" en Swagger UI.
 * @Operation describe cada endpoint (resumen + descripción detallada).
 * @ApiResponses documenta todos los códigos de respuesta posibles (U11 §7.3).
 *
 * Clean Code (U11 §3.2): cada método tiene una sola responsabilidad.
 * Los catch de BadCredentials/Disabled no están aquí — los maneja
 * ApiExceptionHandler (SRP: el controlador no gestiona errores).
 *
 * NOTA: las excepciones de autenticación siguen propagándose a ApiExceptionHandler
 * porque ResponseEntity<?> captura primero. Los catch locales los mantenemos
 * para poder retornar el tipo correcto de ResponseEntity con ApiError.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name        = "Autenticación",
    description = "Endpoints para login con JWT y registro de nuevos usuarios. "
                + "No requieren autenticación previa."
)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils              jwtUtils;
    private final UserService           userService;

    // ── POST /api/auth/login ──────────────────────────────────────────────

    @Operation(
        summary     = "Iniciar sesión",
        description = "Autentica email y contraseña con BCrypt. "
                    + "Si son válidas, retorna un JWT firmado con HS256 válido por 24h. "
                    + "El token debe incluirse en peticiones posteriores como: "
                    + "Authorization: Bearer <token>",
        security    = {}   // este endpoint NO requiere autenticación previa
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "Login exitoso — JWT retornado",
            content      = @Content(schema = @Schema(implementation = JwtResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description  = "Credenciales inválidas o cuenta desactivada",
            content      = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "415",
            description  = "Content-Type debe ser application/json",
            content      = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email    = credenciales.get("email");
        String password = credenciales.get("password");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User usuario = userService.buscarPorEmail(userDetails.getUsername());
            String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol());
            log.info("JWT generado para usuario: {}", email);

            return ResponseEntity.ok(new JwtResponseDto(token, usuario.getEmail(), usuario.getRol()));

        } catch (DisabledException e) {
            log.warn("Login con cuenta desactivada: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiError.unauthorized("Credenciales inválidas o cuenta inactiva", "/api/auth/login"));

        } catch (BadCredentialsException e) {
            log.warn("Credenciales incorrectas para: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiError.unauthorized("Credenciales inválidas o cuenta inactiva", "/api/auth/login"));
        }
    }

    // ── POST /api/auth/registro ───────────────────────────────────────────

    @Operation(
        summary     = "Registrar nuevo usuario",
        description = "Crea una cuenta nueva con BCrypt y retorna un JWT directo. "
                    + "El cliente queda autenticado sin necesitar un login posterior.",
        security    = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description  = "Usuario registrado — JWT retornado",
            content      = @Content(schema = @Schema(implementation = JwtResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description  = "Datos de registro inválidos (contraseñas no coinciden, campos vacíos)",
            content      = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description  = "El email ya está registrado",
            content      = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
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
                    .body(ApiError.conflict(e.getMessage(), "/api/auth/registro"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiError.badRequest(e.getMessage(), "/api/auth/registro"));
        }
    }
}
