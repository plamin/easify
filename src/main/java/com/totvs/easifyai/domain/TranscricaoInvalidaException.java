package com.totvs.easifyai.domain;

/**
 * Lançada quando a transcrição recebida não atende às regras mínimas para ser
 * analisada (ausente, vazia ou curta demais).
 */
public class TranscricaoInvalidaException extends RuntimeException {

    public TranscricaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
