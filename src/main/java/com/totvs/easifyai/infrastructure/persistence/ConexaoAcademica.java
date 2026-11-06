package com.totvs.easifyai.infrastructure.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Perfil opcional para o critério acadêmico. Nunca usa a conta pessoal da FIAP. */
@Component
@Profile("academico")
public class ConexaoAcademica extends ConexaoBanco {
    public static final String USUARIO = "EASIFY_ACADEMICO";
    public static final String SENHA = "Easify_Academico_Demo";

    public ConexaoAcademica(@Value("${spring.datasource.url}") String url) {
        super(url, USUARIO, SENHA);
    }
}
