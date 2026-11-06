package com.totvs.easifyai.domain;

/**
 * Porta do domínio para a análise de transcrições por Inteligência Artificial.
 *
 * <p>Existe para desacoplar a aplicação do provedor concreto de IA. Hoje a
 * implementação é a Gemini (na camada de infraestrutura); amanhã poderia ser
 * outro provedor, ou uma implementação falsa nos testes, sem que a camada de
 * aplicação mude.
 */
public interface Analisador {

    /**
     * Analisa uma transcrição já validada e devolve o resultado no formato do
     * domínio.
     *
     * @throws AnaliseIndisponivelException quando o provedor de IA falha ou
     *                                      devolve uma resposta que não pode ser
     *                                      interpretada.
     */
    AnaliseTranscricao analisarTranscricao(String transcricao);
}
