package com.totvs.easifyai.infrastructure.persistence;

/** Falha técnica de armazenamento; detalhes SQL não devem ir para a API. */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
