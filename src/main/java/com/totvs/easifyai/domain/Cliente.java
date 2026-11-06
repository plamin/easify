package com.totvs.easifyai.domain;

/**
 * Cliente B2B envolvido na conversa analisada.
 *
 * <p>É o participante em torno do qual gira a análise: as dores, o risco de
 * churn e a recomendação de produto dizem respeito a ele.
 */
public class Cliente extends Pessoa {

    private static final String EMPRESA_NAO_INFORMADA = "empresa não informada";

    private final String empresa;

    public Cliente(String nome, String empresa) {
        super(nome);
        this.empresa = (empresa == null || empresa.isBlank())
                ? EMPRESA_NAO_INFORMADA
                : empresa.trim();
    }

    public String getEmpresa() {
        return empresa;
    }

    @Override
    public String descreverPapel() {
        return "cliente da " + empresa;
    }
}
