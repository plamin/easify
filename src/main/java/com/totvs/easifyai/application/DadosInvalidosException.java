package com.totvs.easifyai.application;

/** Entrada recusada pelo caso de uso antes de acessar a persistência. */
public class DadosInvalidosException extends RuntimeException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
