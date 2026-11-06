package com.totvs.easifyai.web;

import java.time.Instant;

/**
 * Formato único de erro devolvido pela API. Sem detalhes internos ou stack
 * trace, apenas o que o cliente precisa para entender o que aconteceu.
 */
public record ErroResposta(String erro, String mensagem, Instant momento) {

    public static ErroResposta de(String erro, String mensagem) {
        return new ErroResposta(erro, mensagem, Instant.now());
    }
}
