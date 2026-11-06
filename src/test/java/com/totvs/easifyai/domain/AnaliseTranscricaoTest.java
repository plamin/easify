package com.totvs.easifyai.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnaliseTranscricaoTest {

    private AnaliseTranscricao analiseCom(NivelInteresse interesse,
                                          RiscoChurn risco,
                                          List<String> concorrentes) {
        return new AnaliseTranscricao(
                new Cliente("Ana", "ACME"),
                new Vendedor("Bruno"),
                List.of("integração lenta"),
                "EasifyAI Pro",
                interesse,
                risco,
                concorrentes,
                "enviar proposta",
                "cliente avaliando");
    }

    @Test
    void riscoAltoExigeAtencaoImediata() {
        AnaliseTranscricao analise = analiseCom(NivelInteresse.BAIXO, RiscoChurn.ALTO, List.of());

        assertThat(analise.exigeAtencaoImediata()).isTrue();
        assertThat(analise.possuiRiscoElevado()).isTrue();
    }

    @Test
    void interesseAltoComConcorrenteNaMesaExigeAtencaoImediata() {
        AnaliseTranscricao analise =
                analiseCom(NivelInteresse.ALTO, RiscoChurn.BAIXO, List.of("ConcorrenteX"));

        assertThat(analise.mencionaConcorrentes()).isTrue();
        assertThat(analise.exigeAtencaoImediata()).isTrue();
    }

    @Test
    void cenarioTranquiloNaoExigeAtencaoImediata() {
        AnaliseTranscricao analise =
                analiseCom(NivelInteresse.MEDIO, RiscoChurn.BAIXO, List.of());

        assertThat(analise.exigeAtencaoImediata()).isFalse();
    }

    @Test
    void descreverConversaUsaOsPapeisDosParticipantes() {
        AnaliseTranscricao analise =
                analiseCom(NivelInteresse.MEDIO, RiscoChurn.BAIXO, List.of());

        assertThat(analise.descreverConversa())
                .isEqualTo("Bruno (vendedor responsável pelo atendimento) "
                        + "e Ana (cliente da ACME)");
    }

    @Test
    void clienteEhObrigatorio() {
        assertThatThrownBy(() -> new AnaliseTranscricao(
                null, new Vendedor("Bruno"), List.of(), "", NivelInteresse.BAIXO,
                RiscoChurn.BAIXO, List.of(), "", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listasNulasViramListasVaziasImutaveis() {
        AnaliseTranscricao analise = new AnaliseTranscricao(
                new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                null, null, NivelInteresse.BAIXO, RiscoChurn.BAIXO, null, null, null);

        assertThat(analise.getDores()).isEmpty();
        assertThat(analise.getConcorrentes()).isEmpty();
        assertThatThrownBy(() -> analise.getDores().add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
