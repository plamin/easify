package com.totvs.easifyai.domain;

/**
 * Vendedor responsável pelo atendimento na conversa analisada.
 *
 * <p>Segunda especialização de {@link Pessoa}. Existe porque toda transcrição
 * do EasifyAI é um diálogo entre vendedor e cliente, e registrar quem conduziu
 * o atendimento faz parte do contexto da análise.
 */
public class Vendedor extends Pessoa {

    public Vendedor(String nome) {
        super(nome);
    }

    @Override
    public String descreverPapel() {
        return "vendedor responsável pelo atendimento";
    }
}
