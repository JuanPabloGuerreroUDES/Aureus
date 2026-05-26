package com.aureus.config;

import com.aureus.security.AureusUserDetailsService;
import com.aureus.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configuración de Spring Security — Unidad 9 completa.
 *
 * ARQUITECTURA DE DOS CADENAS (U9 §3.1 + §7.3):
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Cadena 1: /api/**  @Order(1)                               │
 * │  • Stateless (no sesión)           U9 §7.3                  │
 * │  • JWT en header Authorization     U9 §7.2                  │
 * │  • CSRF desactivado (no cookies)   U9 §6.1                  │
 * │  • JwtAuthFilter antes de UsernamePasswordAuth              │
 * ├─────────────────────────────────────────────────────────────┤
 * │  Cadena 2: /** (MVC)  @Order(2)                             │
 * │  • Stateful (HttpSession)          U9 §7.3                  │
 * │  • Formulario login JSP            U9 §3.3                  │
 * │  • CSRF habilitado (cookies)       U9 §6.1                  │
 * │  • Session fixation protection     U9 §6.2                  │
 * │  • Cabeceras de seguridad HTTP     U9 §6.3                  │
 * └─────────────────────────────────────────────────────────────┘
 *
 * @EnableMethodSecurity habilita @PreAuthorize / @PostAuthorize (U9 §5.2).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // habilita @PreAuthorize, @PostAuthorize, @Secured (U9 §5.2)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AureusUserDetailsService userDetailsService;
    private final JwtAuthFilter            jwtAuthFilter;

    // ── Beans de autenticación compartidos ───────────────────────────────

    /**
     * BCryptPasswordEncoder con factor de costo 12.
     * 2^12 = 4096 iteraciones — resistente a ataques de fuerza bruta (U9 §4.1).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider conecta Spring Security con la BD:
     *   loadUserByUsername() → verifica con BCrypt → genera Authentication (U9 §4.3).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // true: no revelar si el email existe — previene enumeración de usuarios (A07 OWASP)
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    /**
     * AuthenticationManager expuesto como bean para que AuthRestController
     * pueda autenticar credenciales programáticamente en el flujo JWT (U9 §7.2 Paso 2).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ── CADENA 1: API REST — Stateless con JWT (/api/**) ─────────────────

    /**
     * SecurityFilterChain para /api/**.
     *
     * Características (U9 §7.2 + §6.1):
     *   - SessionCreationPolicy.STATELESS: no crea ni usa HttpSession.
     *   - CSRF desactivado: las peticiones API usan Authorization header,
     *     no cookies, por lo que los ataques CSRF no aplican.
     *   - JwtAuthFilter intercepta antes de UsernamePasswordAuthenticationFilter.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")            // solo afecta a rutas /api/**
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // login y registro: públicos
                .anyRequest().authenticated()                   // resto de /api/**: requiere JWT
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // sin HttpSession
            )
            .csrf(csrf -> csrf.disable())           // seguro: no hay cookies de sesión en /api
            .authenticationProvider(authenticationProvider())
            // JwtAuthFilter extrae y valida el Bearer token antes del filtro estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── CADENA 2: MVC — Stateful con sesión y JSP ─────────────────────────

    /**
     * SecurityFilterChain para la aplicación web MVC (JSP).
     *
     * Implementa todos los controles de U9:
     *   §3.3  — SecurityFilterChain con formLogin y logout
     *   §5.1  — Autorización por URL (hasRole / authenticated)
     *   §6.1  — CSRF habilitado (Spring Security por defecto)
     *   §6.2  — Gestión de sesiones: sessionFixation + maximumSessions
     *   §6.3  — Cabeceras de seguridad HTTP: CSP, X-Frame-Options, etc.
     *
     * CORRECCIÓN ERR_TOO_MANY_REDIRECTS:
     *   DispatcherType.FORWARD/ERROR permiten que Spring MVC haga forwards
     *   a los JSPs en /WEB-INF/views/ sin que Spring Security los intercepte.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain mvcFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorización por URL (U9 §5.1) ───────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (U9 §3.3)
                .requestMatchers(
                    "/", "/auth/**",
                    "/static/**", "/css/**", "/js/**",
                    "/images/**", "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()

                // Swagger UI y OpenAPI spec — públicos en desarrollo (U11 §7.1)
                // En producción, considerar restringir a ADMIN o red interna.
                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/api-docs",        "/api-docs/**",
                    "/v3/api-docs",     "/v3/api-docs/**"
                ).permitAll()

                // Solo ADMIN (U9 §5.1 — hasRole)
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // ── Formulario de login (U9 §3.3) ────────────────────────────
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/do-login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )

            // ── Logout (U9 §3.3) ──────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)             // invalida la sesión del servidor
                .deleteCookies("JSESSIONID")             // elimina la cookie de sesión
                .permitAll()
            )

            // ── Gestión de sesiones (U9 §6.2) ────────────────────────────
            .sessionManagement(session -> session
                // Crea nueva sesión al autenticar — previene session fixation attack
                .sessionFixation(fix -> fix.newSession())
                // Máximo 1 sesión por usuario; si inicia otra, la anterior se invalida
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)         // false: nueva sesión desplaza a la vieja
            )

            // ── Cabeceras de seguridad HTTP (U9 §6.3) ────────────────────
            .headers(headers -> headers
                // X-Frame-Options: SAMEORIGIN — protege contra clickjacking
                // SAMEORIGIN en lugar de DENY para permitir iframes del mismo dominio (h2-console)
                .frameOptions(f -> f.sameOrigin())

                // X-Content-Type-Options: nosniff — impide MIME sniffing (U9 §6.3)
                .contentTypeOptions(c -> {})

                // Referrer-Policy: misma-origen — no filtra URL completa a sitios externos
                .referrerPolicy(rp ->
                    rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))

                // Content-Security-Policy (U9 §2.1 — mitigación XSS)
                // Permite scripts e imágenes del mismo origen + CDNs usados en las JSPs
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                    "img-src 'self' data:; " +
                    "font-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                    "frame-ancestors 'self';"          // reemplaza X-Frame-Options en CSP3
                ))
            )

            // ── CSRF habilitado por defecto (U9 §6.1) ────────────────────
            // Spring Security incluye protección CSRF automáticamente.
            // Thymeleaf incluye el token _csrf en todos los formularios con th:action
            // automáticamente — no es necesario añadirlo manualmente (U9 §6.1).

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
