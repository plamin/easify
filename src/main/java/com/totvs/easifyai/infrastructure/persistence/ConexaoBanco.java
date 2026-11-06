package com.totvs.easifyai.infrastructure.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/** Fábrica JDBC com configuração externa. O schema é provisionado separadamente. */
@Component
@Profile("!academico")
public class ConexaoBanco {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoBanco(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String usuario,
            @Value("${spring.datasource.password}") String senha) {
        this.url = Objects.requireNonNull(url);
        this.usuario = Objects.requireNonNull(usuario);
        this.senha = Objects.requireNonNull(senha);
    }

    /** Cada operação fecha sua conexão com try-with-resources. */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}