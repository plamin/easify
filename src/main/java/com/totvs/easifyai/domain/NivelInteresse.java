package com.totvs.easifyai.domain;

import java.util.Locale;

/**
 * Objeto de valor que representa o interesse demonstrado pelo cliente.
 *
 * <p>É um conjunto fechado de possibilidades do negócio. Concentrar aqui a
 * interpretação do texto livre devolvido pela IA evita comparações de string
 * espalhadas pelo código ("alto", "média"...) e dá um tipo seguro ao domínio.
 */
public enum NivelInteresse {

    BAIXO,
    MEDIO,
    ALTO,
    NAO_IDENTIFICADO;

    /**
     * Converte o texto livre da IA para um nível conhecido.
     * Valores ausentes ou não reconhecidos viram {@link #NAO_IDENTIFICADO}.
     */
    public static NivelInteresse fromTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return NAO_IDENTIFICADO;
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "alto", "alta" -> ALTO;
            case "medio", "médio", "média", "media", "moderado", "moderada" -> MEDIO;
            case "baixo", "baixa" -> BAIXO;
            default -> NAO_IDENTIFICADO;
        };
    }

    public boolean isAlto() {
        return this == ALTO;
    }
}
