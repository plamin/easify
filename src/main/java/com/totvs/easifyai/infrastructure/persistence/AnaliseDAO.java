package com.totvs.easifyai.infrastructure.persistence;

import com.totvs.easifyai.domain.AnaliseRepository;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.Cliente;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import com.totvs.easifyai.domain.Vendedor;
import org.springframework.stereotype.Repository;

import com.totvs.easifyai.domain.StatusAcompanhamento;
import com.totvs.easifyai.domain.RegistroAcompanhamento;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Implementação JDBC do armazenamento do agregado análise e suas coleções. */
@Repository
public class AnaliseDAO implements AnaliseRepository {
    private static final String COLUNAS = """
            uuid_analise, cliente_nome, cliente_empresa, vendedor_nome, produto_recomendado,
            nivel_interesse, risco_churn, proxima_acao, resumo
            """;
    private static final String INSERIR = "INSERT INTO analise (" + COLUNAS
            + ", DATA_CRIACAO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String ATUALIZAR = """
            UPDATE analise SET cliente_nome = ?, cliente_empresa = ?, vendedor_nome = ?,
                produto_recomendado = ?, nivel_interesse = ?, risco_churn = ?,
                proxima_acao = ?, resumo = ? WHERE uuid_analise = ?
            """;

    private final ConexaoBanco banco;

    public AnaliseDAO(ConexaoBanco banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public void inserir(AnaliseTranscricao analise, String transcricaoOriginal) {
        Objects.requireNonNull(analise, "Análise obrigatória.");
        if (transcricaoOriginal == null || transcricaoOriginal.isBlank()) {
            throw new IllegalArgumentException("Transcrição original obrigatória.");
        }
        emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement(INSERIR, new String[]{"ID"})) {
                statement.setString(1, analise.getId().toString());
                preencherDados(statement, analise, 2);
                statement.setTimestamp(10, Timestamp.valueOf(analise.getDataCriacao()));
                statement.executeUpdate();
                try (ResultSet chaves = statement.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new SQLException("O banco não retornou o ID da análise.");
                    }
                    long idBanco = chaves.getLong(1);
                    inserirColecoes(conexao, analise, idBanco);
                    try (PreparedStatement transcricao = conexao.prepareStatement("""
                            INSERT INTO TRANSCRICAO (ANALISE_ID, TEXTO_ORIGINAL, DATA_REGISTRO)
                            VALUES (?, ?, CURRENT_TIMESTAMP)
                            """)) {
                        transcricao.setLong(1, idBanco);
                        transcricao.setClob(2, new StringReader(transcricaoOriginal));
                        transcricao.executeUpdate();
                    }
                }
            }
            return null;
        });
    }

    @Override
    public Optional<AnaliseTranscricao> buscarPorId(UUID id) {
        Objects.requireNonNull(id, "ID obrigatório.");
        return emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement(
                    "SELECT id, data_criacao, " + COLUNAS + " FROM analise WHERE uuid_analise = ?")) {
                statement.setString(1, id.toString());
                try (ResultSet resultado = statement.executeQuery()) {
                    return resultado.next() ? Optional.of(mapear(conexao, resultado)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<AnaliseTranscricao> listar() {
        return emTransacao(conexao -> {
            List<AnaliseTranscricao> analises = new ArrayList<>();
            try (PreparedStatement statement = conexao.prepareStatement(
                    "SELECT id, data_criacao, " + COLUNAS + " FROM analise ORDER BY data_criacao DESC, id DESC");
                 ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    analises.add(mapear(conexao, resultado));
                }
            }
            return List.copyOf(analises);
        });
    }

    @Override
    public boolean atualizar(AnaliseTranscricao analise) {
        Objects.requireNonNull(analise, "Análise obrigatória.");
        return emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement(ATUALIZAR)) {
                preencherDados(statement, analise, 1);
                statement.setString(9, analise.getId().toString());
                if (statement.executeUpdate() == 0) {
                    return false;
                }
            }
            long idBanco = buscarIdBanco(conexao, analise.getId()).orElseThrow();
            excluirItens(conexao, "analise_dor", idBanco);
            excluirItens(conexao, "analise_concorrente", idBanco);
            inserirColecoes(conexao, analise, idBanco);
            return true;
        });
    }

    @Override
    public boolean excluir(UUID id) {
        Objects.requireNonNull(id, "ID obrigatório.");
        return emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement("DELETE FROM analise WHERE uuid_analise = ?")) {
                statement.setString(1, id.toString());
                // As chaves estrangeiras removem as coleções na mesma transação.
                return statement.executeUpdate() == 1;
            }
        });
    }

    private void preencherDados(PreparedStatement statement, AnaliseTranscricao analise, int inicio)
            throws SQLException {
        statement.setString(inicio, analise.getCliente().getNome());
        statement.setString(inicio + 1, analise.getCliente().getEmpresa());
        statement.setString(inicio + 2, analise.getVendedor().getNome());
        statement.setClob(inicio + 3, new StringReader(analise.getProdutoRecomendado()));
        statement.setString(inicio + 4, analise.getNivelInteresse().name());
        statement.setString(inicio + 5, analise.getRiscoChurn().name());
        statement.setClob(inicio + 6, new StringReader(analise.getProximaAcao()));
        statement.setClob(inicio + 7, new StringReader(analise.getResumo()));
    }

    private void inserirColecoes(Connection conexao, AnaliseTranscricao analise, long idBanco) throws SQLException {
        inserirItens(conexao, "analise_dor", idBanco, analise.getDores());
        inserirItens(conexao, "analise_concorrente", idBanco, analise.getConcorrentes());
    }

    // O nome da tabela vem somente das constantes internas; todos os valores são parâmetros JDBC.
    private void inserirItens(Connection conexao, String tabela, long id, List<String> itens)
            throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(
                "INSERT INTO " + tabela + " (analise_id, posicao, descricao) VALUES (?, ?, ?)")) {
            for (int posicao = 0; posicao < itens.size(); posicao++) {
                statement.setLong(1, id);
                statement.setInt(2, posicao);
                statement.setClob(3, new StringReader(itens.get(posicao)));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void excluirItens(Connection conexao, String tabela, long id) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(
                "DELETE FROM " + tabela + " WHERE analise_id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private List<String> lerItens(Connection conexao, String tabela, long id) throws SQLException {
        List<String> itens = new ArrayList<>();
        try (PreparedStatement statement = conexao.prepareStatement(
                "SELECT descricao FROM " + tabela + " WHERE analise_id = ? ORDER BY posicao")) {
            statement.setLong(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    itens.add(resultado.getString("descricao"));
                }
            }
        }
        return itens;
    }

    private AnaliseTranscricao mapear(Connection conexao, ResultSet resultado) throws SQLException {
        UUID id = UUID.fromString(resultado.getString("uuid_analise"));
        long idBanco = resultado.getLong("id");
        return new AnaliseTranscricao(id,
                new Cliente(resultado.getString("cliente_nome"), resultado.getString("cliente_empresa")),
                new Vendedor(resultado.getString("vendedor_nome")),
                lerItens(conexao, "analise_dor", idBanco),
                resultado.getString("produto_recomendado"),
                NivelInteresse.valueOf(resultado.getString("nivel_interesse")),
                RiscoChurn.valueOf(resultado.getString("risco_churn")),
                lerItens(conexao, "analise_concorrente", idBanco),
                resultado.getString("proxima_acao"), resultado.getString("resumo"),
                resultado.getTimestamp("data_criacao").toLocalDateTime());
    }

    private Optional<Long> buscarIdBanco(Connection conexao, UUID id) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(
                "SELECT ID FROM ANALISE WHERE UUID_ANALISE = ?")) {
            statement.setString(1, id.toString());
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? Optional.of(resultado.getLong(1)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<String> buscarTranscricao(UUID id) {
        Objects.requireNonNull(id, "ID obrigatório.");
        return emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement("""
                    SELECT T.TEXTO_ORIGINAL FROM TRANSCRICAO T
                    JOIN ANALISE A ON A.ID = T.ANALISE_ID WHERE A.UUID_ANALISE = ?
                    """)) {
                statement.setString(1, id.toString());
                try (ResultSet resultado = statement.executeQuery()) {
                    return resultado.next() ? Optional.of(resultado.getString(1)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public boolean registrarStatus(UUID id, StatusAcompanhamento status, String observacao) {
        Objects.requireNonNull(id, "ID obrigatório.");
        Objects.requireNonNull(status, "Status obrigatório.");
        return emTransacao(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement("""
                    INSERT INTO STATUS_ACOMPANHAMENTO (ANALISE_ID, STATUS, OBSERVACAO, DATA_MUDANCA)
                    SELECT ID, ?, ?, CURRENT_TIMESTAMP FROM ANALISE WHERE UUID_ANALISE = ?
                    """)) {
                statement.setString(1, status.name());
                statement.setString(2, observacao);
                statement.setString(3, id.toString());
                return statement.executeUpdate() == 1;
            }
        });
    }

    @Override
    public List<RegistroAcompanhamento> listarHistorico(UUID id) {
        Objects.requireNonNull(id, "ID obrigatório.");
        return emTransacao(conexao -> {
            List<RegistroAcompanhamento> historico = new ArrayList<>();
            try (PreparedStatement statement = conexao.prepareStatement("""
                    SELECT S.STATUS, S.OBSERVACAO, S.DATA_MUDANCA FROM STATUS_ACOMPANHAMENTO S
                    JOIN ANALISE A ON A.ID = S.ANALISE_ID WHERE A.UUID_ANALISE = ?
                    ORDER BY S.DATA_MUDANCA, S.ID
                    """)) {
                statement.setString(1, id.toString());
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        historico.add(new RegistroAcompanhamento(
                                StatusAcompanhamento.valueOf(resultado.getString(1)),
                                resultado.getString(2), resultado.getTimestamp(3).toLocalDateTime()));
                    }
                }
            }
            return List.copyOf(historico);
        });
    }

    private <T> T emTransacao(OperacaoSql<T> operacao) {
        try (Connection conexao = banco.abrir()) {
            // Mantém a leitura do registro e das coleções consistente frente a atualizações concorrentes.
            conexao.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException falhaRollback) {
                    e.addSuppressed(falhaRollback);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível concluir a operação no banco de dados.", e);
        }
    }

    @FunctionalInterface
    private interface OperacaoSql<T> {
        T executar(Connection conexao) throws SQLException;
    }
}
