package com.totvs.easifyai.application;

import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.AnaliseRepository;
import com.totvs.easifyai.domain.AnaliseIndisponivelException;
import com.totvs.easifyai.domain.Analisador;
import com.totvs.easifyai.domain.Cliente;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import com.totvs.easifyai.domain.TranscricaoInvalidaException;
import com.totvs.easifyai.domain.Vendedor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AnalisarTranscricaoUseCaseTest {

    /**
     * Implementação falsa da porta {@link Analisador}: mostra que a abstração
     * permite testar o caso de uso sem depender da Gemini.
     */
    private static class AnalisadorFake implements Analisador {
        String transcricaoRecebida;

        @Override
        public AnaliseTranscricao analisarTranscricao(String transcricao) {
            this.transcricaoRecebida = transcricao;
            return new AnaliseTranscricao(
                    new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                    List.of(), "", NivelInteresse.BAIXO, RiscoChurn.BAIXO,
                    List.of(), "", "ok");
        }
    }

    private final AnalisadorFake analisador = new AnalisadorFake();
    private final AnaliseRepository repositorio = mock(AnaliseRepository.class);
    private final AnalisarTranscricaoUseCase useCase = new AnalisarTranscricaoUseCase(analisador, repositorio);

    @Test
    void delegaTranscricaoValidaJaAparadaParaAPorta() {
        AnaliseTranscricao resultado = useCase.executar("   reunião com o cliente sobre renovação do contrato   ");

        assertThat(analisador.transcricaoRecebida)
                .isEqualTo("reunião com o cliente sobre renovação do contrato");
        verify(repositorio).inserir(resultado, "   reunião com o cliente sobre renovação do contrato   ");
    }

    @Test
    void transcricaoVaziaEhRejeitada() {
        assertThatThrownBy(() -> useCase.executar("   "))
                .isInstanceOf(TranscricaoInvalidaException.class);
    }

    @Test
    void transcricaoNulaEhRejeitada() {
        assertThatThrownBy(() -> useCase.executar(null))
                .isInstanceOf(TranscricaoInvalidaException.class);
    }

    @Test
    void transcricaoCurtaDemaisEhRejeitada() {
        assertThatThrownBy(() -> useCase.executar("oi, tudo bem?"))
                .isInstanceOf(TranscricaoInvalidaException.class);
        verifyNoInteractions(repositorio);
    }

    @Test
    void falhaDaIaNaoPersisteAnalise() {
        Analisador indisponivel = texto -> {
            throw new AnaliseIndisponivelException("Indisponível");
        };
        var casoDeUso = new AnalisarTranscricaoUseCase(indisponivel, repositorio);

        assertThatThrownBy(() -> casoDeUso.executar("Transcrição suficientemente longa"))
                .isInstanceOf(AnaliseIndisponivelException.class);
        verifyNoInteractions(repositorio);
    }

    @Test
    void falhaAoSalvarNaoRetornaSucesso() {
        var falha = new IllegalStateException("Armazenamento indisponível");
        doThrow(falha).when(repositorio).inserir(any(), any());

        assertThatThrownBy(() -> useCase.executar("Transcrição suficientemente longa"))
                .isSameAs(falha);
    }
}
