package com.totvs.easifyai.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.totvs.easifyai.domain.AnaliseIndisponivelException;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.Analisador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementação da porta {@link Analisador} usando a API Gemini.
 *
 * <p>Toda a dependência de HTTP, JSON e do formato específico da Gemini fica
 * confinada nesta camada. O restante da aplicação enxerga apenas a abstração
 * {@link Analisador} e o modelo de domínio.
 */
@Component
public class GeminiAnalisador implements Analisador {

    private static final String PROMPT_INSTRUCOES = """
            Você é um especialista em vendas B2B. Analise a transcrição de uma
            conversa entre um vendedor e um cliente e responda APENAS com um JSON
            válido, sem texto antes ou depois, exatamente neste formato:

            {
              "cliente": "nome do cliente",
              "empresaCliente": "empresa do cliente",
              "vendedor": "nome do vendedor",
              "dores": ["dores apresentadas pelo cliente"],
              "produtoRecomendado": "produto mais adequado",
              "nivelInteresse": "baixo | medio | alto",
              "riscoChurn": "baixo | medio | alto",
              "concorrentes": ["concorrentes mencionados"],
              "proximaAcao": "próxima ação comercial recomendada",
              "resumo": "resumo objetivo da conversa"
            }

            Use "" ou [] quando a informação não estiver presente na transcrição.

            Transcrição:
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiRespostaMapper respostaMapper;
    private final String apiKey;
    private final String endpoint;

    public GeminiAnalisador(RestTemplate geminiRestTemplate,
                            GeminiRespostaMapper respostaMapper,
                            @Value("${gemini.api.key:}") String apiKey,
                            @Value("${gemini.api.model:gemini-2.5-flash}") String model) {
        this.restTemplate = geminiRestTemplate;
        this.respostaMapper = respostaMapper;
        this.apiKey = apiKey;
        this.endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";
    }

    @Override
    public AnaliseTranscricao analisarTranscricao(String transcricao) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AnaliseIndisponivelException(
                    "Chave da API Gemini não configurada (variável GEMINI_API_KEY).");
        }

        String textoResposta = extrairTexto(chamarGemini(transcricao));
        GeminiAnaliseResponse resposta = desserializar(textoResposta);
        return respostaMapper.paraDominio(resposta);
    }

    private String chamarGemini(String transcricao) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<String> request = new HttpEntity<>(montarCorpo(transcricao), headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AnaliseIndisponivelException(
                        "A Gemini respondeu com status " + response.getStatusCode() + ".");
            }
            return response.getBody();

        } catch (RestClientException e) {
            throw new AnaliseIndisponivelException(
                    "Falha na comunicação com a Gemini.", e);
        }
    }

    private String montarCorpo(String transcricao) {
        try {
            ObjectNode texto = objectMapper.createObjectNode()
                    .put("text", PROMPT_INSTRUCOES + transcricao);

            ObjectNode parte = objectMapper.createObjectNode();
            parte.putArray("parts").add(texto);

            ObjectNode corpo = objectMapper.createObjectNode();
            corpo.putArray("contents").add(parte);

            return objectMapper.writeValueAsString(corpo);
        } catch (Exception e) {
            throw new AnaliseIndisponivelException(
                    "Não foi possível montar a requisição para a Gemini.", e);
        }
    }

    private String extrairTexto(String corpoResposta) {
        try {
            JsonNode texto = objectMapper.readTree(corpoResposta)
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text");

            if (texto.isMissingNode() || texto.asText().isBlank()) {
                throw new AnaliseIndisponivelException(
                        "A Gemini não retornou conteúdo de análise.");
            }
            return limparCercasDeCodigo(texto.asText());

        } catch (AnaliseIndisponivelException e) {
            throw e;
        } catch (Exception e) {
            throw new AnaliseIndisponivelException(
                    "Resposta da Gemini em formato inesperado.", e);
        }
    }

    private GeminiAnaliseResponse desserializar(String json) {
        try {
            return objectMapper.readValue(json, GeminiAnaliseResponse.class);
        } catch (Exception e) {
            throw new AnaliseIndisponivelException(
                    "A Gemini retornou um JSON de análise inválido.", e);
        }
    }

    private static String limparCercasDeCodigo(String texto) {
        return texto.replace("```json", "").replace("```", "").trim();
    }
}
