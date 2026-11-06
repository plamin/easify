package com.totvs.easifyai.domain;

/**
 * Conceito base do domínio para quem participa de uma conversa comercial.
 *
 * <p>É abstrata de propósito: no EasifyAI uma "pessoa genérica" não existe,
 * sempre será um {@link Cliente} ou um {@link Vendedor}. O método
 * {@link #descreverPapel()} é abstrato para que cada especialização explique,
 * do seu jeito, qual papel exerce na negociação (polimorfismo).
 */
public abstract class Pessoa {

    protected final String nome;

    protected Pessoa(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da pessoa é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    /**
     * Descreve o papel que a pessoa exerce na conversa comercial.
     * Cada subclasse implementa de acordo com o seu contexto.
     */
    public abstract String descreverPapel();

    /**
     * Apresentação legível da pessoa. Usa {@link #descreverPapel()}, portanto
     * o resultado varia conforme a subclasse concreta em tempo de execução.
     */
    public String apresentar() {
        return nome + " (" + descreverPapel() + ")";
    }
}
