package com.aureus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración adicional de Spring MVC.
 *
 * HiddenHttpMethodFilter: permite que formularios HTML usen métodos
 * HTTP como PUT y DELETE mediante un campo oculto _method.
 * Necesario porque los formularios HTML solo soportan GET y POST.
 *
 * Ejemplo de uso en JSP:
 *   <form action="/recurso/1" method="post">
 *     <input type="hidden" name="_method" value="DELETE">
 *     ...
 *   </form>
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
