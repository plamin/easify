package com.totvs.easifyai.web;

/**
 * Corpo da requisição para análise de uma transcrição.
 * Modelo exclusivo da camada HTTP, separado do domínio.
 */
public class AnalisarTranscricaoRequest {

    private String transcricao;

    public String getTranscricao() {
        return transcricao;
    }

    public void setTranscricao(String transcricao) {
        this.transcricao = transcricao;
    }
}
