package com.totvs.easifyai.application;

import com.totvs.easifyai.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/** Casos de uso de consulta, edição, exclusão e acompanhamento do histórico. */
@Service
public class GerenciarAnalisesService {
    private final AnaliseRepository analises;

    public GerenciarAnalisesService(AnaliseRepository analises) {
        this.analises = analises;
    }

    public List<AnaliseTranscricao> listar() {
        return analises.listar();
    }

    public AnaliseTranscricao buscar(UUID id) {
        validarId(id);
        return analises.buscarPorId(id).orElseThrow(AnaliseNaoEncontradaException::new);
    }

    public AnaliseTranscricao atualizar(UUID id, AtualizarAnaliseCommand dados) {
        validarId(id);
        ValidacaoAnalise.validar(dados);
        AnaliseTranscricao atual = buscar(id);
        AnaliseTranscricao corrigida = new AnaliseTranscricao(id,
                new Cliente(dados.cliente(), dados.empresaCliente()), new Vendedor(dados.vendedor()),
                dados.dores(), dados.produtoRecomendado(), dados.nivelInteresse(), dados.riscoChurn(),
                dados.concorrentes(), dados.proximaAcao(), dados.resumo(), atual.getDataCriacao());
        if (!analises.atualizar(corrigida)) {
            throw new AnaliseNaoEncontradaException();
        }
        return corrigida;
    }

    public void excluir(UUID id) {
        validarId(id);
        if (!analises.excluir(id)) {
            throw new AnaliseNaoEncontradaException();
        }
    }

    public String buscarTranscricao(UUID id) {
        buscar(id);
        return analises.buscarTranscricao(id).orElseThrow(() ->
                new IllegalStateException("Análise persistida sem transcrição original."));
    }

    public List<RegistroAcompanhamento> listarHistorico(UUID id) {
        buscar(id);
        return analises.listarHistorico(id);
    }

    public void registrarStatus(UUID id, StatusAcompanhamento status, String observacao) {
        validarId(id);
        if (status == null) {
            throw new DadosInvalidosException("Status obrigatório.");
        }
        ValidacaoAnalise.validarObservacao(observacao);
        if (!analises.registrarStatus(id, status, observacao)) {
            throw new AnaliseNaoEncontradaException();
        }
    }

    private static void validarId(UUID id) {
        if (id == null) {
            throw new DadosInvalidosException("ID obrigatório.");
        }
    }
}
