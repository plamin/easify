package com.totvs.easifyai.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Cenários comerciais exercitam os quatro métodos com objetos reais, sem IA ou banco. */
class RegrasComerciaisTest {
    private AnaliseTranscricao analise(NivelInteresse interesse, RiscoChurn risco,
                                       boolean concorrencia, List<String> dores, String produto) {
        return new AnaliseTranscricao(new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                dores, produto, interesse, risco, concorrencia ? List.of("Concorrente A") : List.of(),
                "Sugestão textual da Gemini", "Reunião comercial");
    }

    @ParameterizedTest(name = "{0}/{1}, concorrência={2}: prioridade {3}, ação {4}")
    @CsvSource({
            "ALTO, BAIXO, false, ALTA, ENVIAR_PROPOSTA, true",
            "ALTO, BAIXO, true, IMEDIATA, DIFERENCIAR_PROPOSTA, true",
            "ALTO, MEDIO, false, ALTA, TRATAR_OBJECOES, false",
            "ALTO, MEDIO, true, IMEDIATA, TRATAR_OBJECOES, false",
            "ALTO, ALTO, true, IMEDIATA, PLANEJAR_RETENCAO, false",
            "MEDIO, BAIXO, false, MEDIA, APRESENTAR_SOLUCAO, false",
            "MEDIO, BAIXO, true, MEDIA, APRESENTAR_SOLUCAO, false",
            "MEDIO, MEDIO, false, MEDIA, TRATAR_OBJECOES, false",
            "MEDIO, MEDIO, true, ALTA, TRATAR_OBJECOES, false",
            "BAIXO, BAIXO, false, BAIXA, NUTRIR_RELACIONAMENTO, false",
            "BAIXO, BAIXO, true, BAIXA, NUTRIR_RELACIONAMENTO, false",
            "BAIXO, MEDIO, true, ALTA, TRATAR_OBJECOES, false",
            "BAIXO, ALTO, false, IMEDIATA, PLANEJAR_RETENCAO, false"
    })
    void decideComBaseEmInteresseRiscoEConcorrencia(NivelInteresse interesse, RiscoChurn risco,
                                                   boolean concorrencia, PrioridadeComercial prioridade,
                                                   AcaoComercial acao, boolean proposta) {
        var analise = analise(interesse, risco, concorrencia, List.of("Integração lenta"), "EasifyAI");

        assertThat(analise.identificarPendenciasComerciais()).isEmpty();
        assertThat(analise.podeEnviarProposta()).isEqualTo(proposta);
        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(prioridade);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(acao);
        assertThat(analise.getProximaAcao()).isEqualTo("Sugestão textual da Gemini");
    }

    @Test
    void acumulaTodasAsPendenciasSemTratarDesconhecidoComoBaixo() {
        var analise = analise(NivelInteresse.NAO_IDENTIFICADO, RiscoChurn.NAO_IDENTIFICADO,
                false, null, null);

        assertThat(analise.identificarPendenciasComerciais()).containsExactly(
                PendenciaComercial.INTERESSE_NAO_IDENTIFICADO, PendenciaComercial.RISCO_NAO_IDENTIFICADO,
                PendenciaComercial.DORES_NAO_IDENTIFICADAS, PendenciaComercial.PRODUTO_NAO_RECOMENDADO);
        assertThat(analise.podeEnviarProposta()).isFalse();
        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(PrioridadeComercial.MEDIA);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
        assertThatThrownBy(() -> analise.identificarPendenciasComerciais().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "NAO_IDENTIFICADO, BAIXO, INTERESSE_NAO_IDENTIFICADO, MEDIA",
            "ALTO, NAO_IDENTIFICADO, RISCO_NAO_IDENTIFICADO, ALTA"
    })
    void umaClassificacaoDesconhecidaImpedeProposta(NivelInteresse interesse, RiscoChurn risco,
                                                   PendenciaComercial pendencia, PrioridadeComercial prioridade) {
        var analise = analise(interesse, risco, false, List.of("Integração lenta"), "EasifyAI");

        assertThat(analise.identificarPendenciasComerciais()).containsExactly(pendencia);
        assertThat(analise.podeEnviarProposta()).isFalse();
        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(prioridade);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
    }

    @Test
    void produtoSozinhoNaoQualificaPropostaSemDorIdentificada() {
        var analise = analise(NivelInteresse.ALTO, RiscoChurn.BAIXO, false,
                Arrays.asList(null, "", "  "), "EasifyAI");

        assertThat(analise.identificarPendenciasComerciais())
                .containsExactly(PendenciaComercial.DORES_NAO_IDENTIFICADAS);
        assertThat(analise.podeEnviarProposta()).isFalse();
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
    }

    @Test
    void dorSozinhaNaoQualificaPropostaSemProdutoRecomendado() {
        var analise = analise(NivelInteresse.ALTO, RiscoChurn.BAIXO, false,
                List.of("Integração lenta"), "  ");

        assertThat(analise.identificarPendenciasComerciais())
                .containsExactly(PendenciaComercial.PRODUTO_NAO_RECOMENDADO);
        assertThat(analise.podeEnviarProposta()).isFalse();
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
    }

    @Test
    void riscoAltoConhecidoPrevaleceSobreLacunasDoDiagnostico() {
        var analise = analise(NivelInteresse.NAO_IDENTIFICADO, RiscoChurn.ALTO, false, null, null);

        assertThat(analise.identificarPendenciasComerciais()).hasSize(3);
        assertThat(analise.podeEnviarProposta()).isFalse();
        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(PrioridadeComercial.IMEDIATA);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.PLANEJAR_RETENCAO);
    }

    @Test
    void concorrenciaUrgenteNaoDispensaCompletarDiagnostico() {
        var analise = analise(NivelInteresse.ALTO, RiscoChurn.NAO_IDENTIFICADO, true, List.of("Preço"), "EasifyAI");

        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(PrioridadeComercial.IMEDIATA);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
        assertThat(analise.podeEnviarProposta()).isFalse();
    }

    @Test
    void interesseBaixoComLacunasExigeDiagnosticoAntesDeNutricao() {
        var analise = analise(NivelInteresse.BAIXO, RiscoChurn.BAIXO, false, List.of(), "");

        assertThat(analise.classificarPrioridadeComercial()).isEqualTo(PrioridadeComercial.MEDIA);
        assertThat(analise.sugerirAcaoComercial()).isEqualTo(AcaoComercial.COMPLETAR_DIAGNOSTICO);
    }
}
