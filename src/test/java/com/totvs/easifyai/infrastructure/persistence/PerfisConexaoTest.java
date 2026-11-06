package com.totvs.easifyai.infrastructure.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PerfisConexaoTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void selecionaUmaUnicaFabricaSemAbrirConexao(boolean academico) {
        try (var contexto = new AnnotationConfigApplicationContext()) {
            if (academico) contexto.getEnvironment().setActiveProfiles("academico");
            contexto.getEnvironment().getPropertySources().addFirst(new MapPropertySource("teste", Map.of(
                    "spring.datasource.url", "jdbc:h2:mem:perfil",
                    "spring.datasource.username", "", "spring.datasource.password", "")));
            contexto.register(ConexaoBanco.class, ConexaoAcademica.class);
            contexto.refresh();
            assertEquals(1, contexto.getBeansOfType(ConexaoBanco.class).size());
            assertEquals(academico ? ConexaoAcademica.class : ConexaoBanco.class,
                    contexto.getBean(ConexaoBanco.class).getClass());
        }
    }
}
