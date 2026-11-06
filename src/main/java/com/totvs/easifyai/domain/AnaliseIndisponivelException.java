package com.totvs.easifyai.domain;

/**
 * Lançada quando não foi possível obter uma análise do provedor de IA:
 * falha de comunicação, timeout, resposta inesperada ou JSON inválido.
 *
 * <p>Vive no domínio porque representa o descumprimento do contrato da porta
 * {@link Analisador}, independente de qual seja a implementação concreta.
 */
public class AnaliseIndisponivelException extends RuntimeException {

    public AnaliseIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public AnaliseIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
