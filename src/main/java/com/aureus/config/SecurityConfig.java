package com.aureus.config;

import com.aureus.security.AureusUserDetailsService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CORRECCIÓN DEFINITIVA del ERR_TOO_MANY_REDIRECTS.
 *
 * CAUSA RAÍZ (confirmada por logs):
 *   Spring MVC hace forward a /WEB-INF/views/auth/login.jsp para renderizar la vista.
 *   Spring Security intercepta ese forward interno como si fuera una nueva petición
 *   HTTP, determina que /WEB-INF/views/auth/login.jsp no está en permitAll(), y
 *   redirige a /auth/login → nuevo forward → bucle infinito.
 *
 * SOLUCIÓN:
 *   Permitir explícitamente los DispatcherType.FORWARD y ERROR para que Spring
 *   Security no intercepte los forwards internos del servidor (renderizado de JSPs).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AureusUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── CRÍTICO: permitir forwards y errores internos del servidor ──
                // Sin esto, Spring Security intercepta el forward de MVC a los JSPs
                // en /WEB-INF/views/ causando el bucle de redirección infinita.
                .dispatcherTypeMatchers(
                    DispatcherType.FORWARD,
                    DispatcherType.INCLUDE,
                    DispatcherType.ERROR
                ).permitAll()

                // ── Rutas públicas (sin autenticación) ─────────────────────────
                .requestMatchers(
                    "/", "/auth/**",
                    "/static/**", "/css/**", "/js/**",
                    "/images/**", "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()

                // ── Solo ADMIN ─────────────────────────────────────────────────
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── Todo lo demás requiere login ────────────────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/do-login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(f -> f.sameOrigin())
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
