package com.aureus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT para la capa REST (/api/**).
 *
 * Implementa el Paso 4-5 del flujo JWT (U9 §7.2):
 *   - El cliente incluye el JWT en: Authorization: Bearer <token>
 *   - Este filtro extrae y valida el token en cada petición.
 *   - Si el token es válido, autentica al usuario en el SecurityContext
 *     sin consultar la base de datos nuevamente (stateless).
 *
 * Se ejecuta una sola vez por petición (OncePerRequestFilter).
 * Solo se registra en la cadena de filtros de /api/** (ver SecurityConfig).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AureusUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            String email = jwtUtils.getEmailFromToken(token);

            // Cargar UserDetails para obtener authorities (roles)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Crear el objeto Authentication y establecerlo en el SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                               // credenciales: null (ya verificadas con JWT)
                            userDetails.getAuthorities());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT válido — usuario autenticado en SecurityContext: {}", email);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del header "Authorization: Bearer <token>".
     * Devuelve null si el header no existe o no tiene el formato esperado.
     *
     * @param request petición HTTP
     * @return token JWT sin el prefijo "Bearer ", o null
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // elimina "Bearer "
        }
        return null;
    }
}
