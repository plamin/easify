package com.totvs.easifyai.infrastructure.gemini;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuração da comunicação HTTP com a Gemini.
 *
 * <p>Isola aqui a criação do {@link RestTemplate} com timeouts explícitos, para
 * que uma indisponibilidade da API não deixe requisições penduradas.
 */
@Configuration
public class GeminiConfig {

    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(5);
    private static final Duration TIMEOUT_LEITURA = Duration.ofSeconds(30);

    @Bean
    public RestTemplate geminiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_CONEXAO);
        factory.setReadTimeout(TIMEOUT_LEITURA);
        return new RestTemplate(factory);
    }
}
