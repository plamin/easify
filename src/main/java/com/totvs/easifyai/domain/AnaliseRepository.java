package com.totvs.easifyai.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Armazenamento de análises, sem expor JDBC ou o banco ao caso de uso. */
public interface AnaliseRepository {
    void inserir(AnaliseTranscricao analise, String transcricaoOriginal);

    Optional<String> buscarTranscricao(UUID id);

    /** Acrescenta um evento, sem sobrescrever o histórico. False se a análise não existe. */
    boolean registrarStatus(UUID id, StatusAcompanhamento status, String observacao);

    List<RegistroAcompanhamento> listarHistorico(UUID id);

    Optional<AnaliseTranscricao> buscarPorId(UUID id);

    List<AnaliseTranscricao> listar();

    /** Retorna false quando a análise não existe; não insere implicitamente. */
    boolean atualizar(AnaliseTranscricao analise);

    /** Retorna false quando a análise já não existe. */
    boolean excluir(UUID id);
}
