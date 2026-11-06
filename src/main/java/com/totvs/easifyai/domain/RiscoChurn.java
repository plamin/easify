package com.totvs.easifyai.domain;

import java.util.Locale;

/**
 * Objeto de valor que representa o risco de perda do cliente (churn).
 *
 * <p>Além de tipar o domínio, carrega a regra de negócio {@link #isCritico()},
 * que antes vivia solta como {@code equalsIgnoreCase("alto")} na camada de
 * serviço.
 */
public enum RiscoChurn {

    BAIXO,
    MEDIO,
    ALTO,
    NAO_IDENTIFICADO;

    public static RiscoChurn fromTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return NAO_IDENTIFICADO;
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "alto", "alta", "elevado", "elevada" -> ALTO;
            case "medio", "médio", "média", "media", "moderado", "moderada" -> MEDIO;
            case "baixo", "baixa" -> BAIXO;
            default -> NAO_IDENTIFICADO;
        };
    }

    /**
     * Indica se o risco exige atenção imediata da equipe comercial.
     */
    public boolean isCritico() {
        return this == ALTO;
    }
}
