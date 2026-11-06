package com.totvs.easifyai.application;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Valida o contrato de edição e os limites BYTE do schema Oracle em AL32UTF8. */
final class ValidacaoAnalise {
    private static final int LIMITE_PARTICIPANTE_BYTES = 500;
    private static final int LIMITE_OBSERVACAO_BYTES = 1000;

    private ValidacaoAnalise() { }

    static void validar(AtualizarAnaliseCommand dados) {
        exigir(dados, "Os dados de atualização são obrigatórios.");
        validarParticipantes(dados.cliente(), dados.empresaCliente(), dados.vendedor());
        exigir(dados.nivelInteresse(), "Nível de interesse obrigatório.");
        exigir(dados.riscoChurn(), "Risco de churn obrigatório.");
        validarLista(dados.dores(), "dores");
        validarLista(dados.concorrentes(), "concorrentes");
        exigir(dados.produtoRecomendado(), "Informe produtoRecomendado, mesmo que vazio.");
        exigir(dados.proximaAcao(), "Informe proximaAcao, mesmo que vazia.");
        exigir(dados.resumo(), "Informe resumo, mesmo que vazio.");
    }

    static void validarParticipantes(String cliente, String empresa, String vendedor) {
        validarNome(cliente, "Cliente");
        validarNome(vendedor, "Vendedor");
        exigir(empresa, "Informe empresaCliente, mesmo que vazia.");
        validarBytes(empresa.trim(), LIMITE_PARTICIPANTE_BYTES, "Empresa");
    }

    static void validarObservacao(String observacao) {
        if (observacao != null) {
            validarBytes(observacao, LIMITE_OBSERVACAO_BYTES, "Observação");
        }
    }

    private static void validarNome(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(campo + " obrigatório.");
        }
        validarBytes(valor.trim(), LIMITE_PARTICIPANTE_BYTES, campo);
    }

    private static void validarBytes(String texto, int limite, String campo) {
        if (texto.getBytes(StandardCharsets.UTF_8).length > limite) {
            throw new DadosInvalidosException(campo + " deve ter no máximo " + limite + " bytes em UTF-8.");
        }
    }

    private static void validarLista(List<String> itens, String campo) {
        exigir(itens, "Informe " + campo + ", mesmo que seja uma lista vazia.");
        if (itens.stream().anyMatch(item -> item == null || item.isBlank())) {
            throw new DadosInvalidosException("A lista " + campo + " não pode conter itens nulos ou vazios.");
        }
    }

    private static void exigir(Object valor, String mensagem) {
        if (valor == null) {
            throw new DadosInvalidosException(mensagem);
        }
    }
}
