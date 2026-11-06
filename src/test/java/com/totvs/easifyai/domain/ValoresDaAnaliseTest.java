package com.totvs.easifyai.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValoresDaAnaliseTest {

    @Test
    void medioComAcentoNaoEhConfundidoComDadoDesconhecido() {
        assertThat(NivelInteresse.fromTexto(" MÉDIO ")).isEqualTo(NivelInteresse.MEDIO);
        assertThat(RiscoChurn.fromTexto(" MÉDIO ")).isEqualTo(RiscoChurn.MEDIO);
    }

    @Test
    void nivelInteresseInterpretaTextoLivreDaIa() {
        assertThat(NivelInteresse.fromTexto("Alto")).isEqualTo(NivelInteresse.ALTO);
        assertThat(NivelInteresse.fromTexto("média")).isEqualTo(NivelInteresse.MEDIO);
        assertThat(NivelInteresse.fromTexto("baixa")).isEqualTo(NivelInteresse.BAIXO);
        assertThat(NivelInteresse.fromTexto(null)).isEqualTo(NivelInteresse.NAO_IDENTIFICADO);
        assertThat(NivelInteresse.fromTexto("qualquer coisa")).isEqualTo(NivelInteresse.NAO_IDENTIFICADO);
    }

    @Test
    void riscoChurnAltoEhCritico() {
        assertThat(RiscoChurn.fromTexto("elevado").isCritico()).isTrue();
        assertThat(RiscoChurn.fromTexto("baixo").isCritico()).isFalse();
        assertThat(RiscoChurn.fromTexto("").isCritico()).isFalse();
    }
}
