package com.totvs.easifyai.infrastructure.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiRespostaMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiRespostaMapper mapper = new GeminiRespostaMapper();

    @Test
    void converteJsonDaIaParaODominio() throws Exception {
        String json = """
                {
                  "cliente": "Ana",
                  "empresaCliente": "ACME",
                  "vendedor": "Bruno",
                  "dores": ["integração lenta", ""],
                  "produtoRecomendado": "EasifyAI Pro",
                  "nivelInteresse": "alto",
                  "riscoChurn": "baixo",
                  "concorrentes": ["ConcorrenteX"],
                  "proximaAcao": "enviar proposta",
                  "resumo": "cliente avaliando",
                  "campoDesconhecido": "ignorado"
                }
                """;

        GeminiAnaliseResponse resposta =
                objectMapper.readValue(json, GeminiAnaliseResponse.class);
        AnaliseTranscricao analise = mapper.paraDominio(resposta);

        assertThat(analise.getCliente().getNome()).isEqualTo("Ana");
        assertThat(analise.getCliente().getEmpresa()).isEqualTo("ACME");
        assertThat(analise.getVendedor().getNome()).isEqualTo("Bruno");
        assertThat(analise.getDores()).containsExactly("integração lenta");
        assertThat(analise.getNivelInteresse()).isEqualTo(NivelInteresse.ALTO);
        assertThat(analise.getRiscoChurn()).isEqualTo(RiscoChurn.BAIXO);
        assertThat(analise.mencionaConcorrentes()).isTrue();
    }

    @Test
    void camposAusentesRecebemPadraoDoDominio() throws Exception {
        GeminiAnaliseResponse resposta =
                objectMapper.readValue("{}", GeminiAnaliseResponse.class);

        AnaliseTranscricao analise = mapper.paraDominio(resposta);

        assertThat(analise.getCliente().getNome()).isEqualTo("Não informado");
        assertThat(analise.getNivelInteresse()).isEqualTo(NivelInteresse.NAO_IDENTIFICADO);
        assertThat(analise.getRiscoChurn()).isEqualTo(RiscoChurn.NAO_IDENTIFICADO);
        assertThat(analise.getDores()).isEmpty();
    }
}
