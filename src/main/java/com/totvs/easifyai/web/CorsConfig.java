package com.totvs.easifyai.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

/** Acesso de navegador opt-in; Postman não precisa de CORS. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] origens;

    public CorsConfig(@Value("${easify.cors.origens:}") String origens) {
        this.origens = Arrays.stream(origens.split(",")).map(String::trim)
                .filter(valor -> !valor.isEmpty()).toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origens.length > 0) {
            registry.addMapping("/**").allowedOrigins(origens)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("Content-Type").exposedHeaders("Location");
        }
    }
}
