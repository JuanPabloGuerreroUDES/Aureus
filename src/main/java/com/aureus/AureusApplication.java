package com.aureus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada principal de Aureus.
 *
 * Extiende SpringBootServletInitializer para permitir despliegue
 * tanto como JAR embebido (./mvnw spring-boot:run) como WAR
 * en un servidor externo (Tomcat, JBoss, etc.).
 */
@SpringBootApplication
public class AureusApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AureusApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(AureusApplication.class, args);
    }
}
