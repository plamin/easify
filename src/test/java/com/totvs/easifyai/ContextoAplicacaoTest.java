package com.totvs.easifyai;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que o contexto Spring sobe e que as camadas estão ligadas
 * corretamente (controller -> caso de uso -> porta -> implementação Gemini).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.datasource.url=jdbc:h2:mem:contexto;MODE=Oracle",
                "spring.datasource.username=sa", "spring.datasource.password=", "gemini.api.key="})
class ContextoAplicacaoTest {

    @Autowired
    private AnalisarTranscricaoUseCase analisarTranscricao;

    @Test
    void contextoCarrega() {
        assertThat(analisarTranscricao).isNotNull();
    }
}
