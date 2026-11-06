package com.totvs.easifyai.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PessoaPolimorfismoTest {

    @Test
    void cadaPapelEhDescritoDeFormaDiferente() {
        Pessoa cliente = new Cliente("Ana", "ACME");
        Pessoa vendedor = new Vendedor("Bruno");

        assertThat(cliente.descreverPapel()).isEqualTo("cliente da ACME");
        assertThat(vendedor.descreverPapel()).isEqualTo("vendedor responsável pelo atendimento");
    }

    @Test
    void apresentarUsaImplementacaoDaSubclasseEmTempoDeExecucao() {
        List<Pessoa> pessoas = List.of(new Vendedor("Bruno"), new Cliente("Ana", "ACME"));

        assertThat(pessoas.stream().map(Pessoa::apresentar).toList())
                .containsExactly(
                        "Bruno (vendedor responsável pelo atendimento)",
                        "Ana (cliente da ACME)");
    }

    @Test
    void clienteSemEmpresaRecebeDescricaoPadrao() {
        assertThat(new Cliente("Ana", "  ").descreverPapel())
                .isEqualTo("cliente da empresa não informada");
    }

    @Test
    void nomeEhObrigatorio() {
        assertThatThrownBy(() -> new Vendedor(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
