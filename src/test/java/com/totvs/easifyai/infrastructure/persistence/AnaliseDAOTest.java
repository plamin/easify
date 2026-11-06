package com.totvs.easifyai.infrastructure.persistence;

import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.Cliente;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import com.totvs.easifyai.domain.Vendedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnaliseDAOTest {
    @TempDir
    Path diretorio;
    private ConexaoBanco banco;
    private AnaliseDAO dao;
    private final String senhaTeste = UUID.randomUUID().toString();
    private static final String TRANSCRICAO = "  Transcrição fictícia de desenvolvimento.\nSegunda linha.  ";

    @BeforeEach
    void prepararBancoRealEmArquivoTemporario() {
        banco = novaConexao();
        BancoTeste.inicializar(banco);
        dao = new AnaliseDAO(banco);
    }

    private ConexaoBanco novaConexao() {
        return new ConexaoBanco("jdbc:h2:file:" + diretorio.resolve("analises").toAbsolutePath() + ";MODE=Oracle",
                "sa", senhaTeste);
    }

    private AnaliseTranscricao exemplo() {
        return new AnaliseTranscricao(new Cliente("Ana d'Ávila", "ACME"), new Vendedor("Bruno"),
                List.of("Integração lenta", "Preço; suporte", "Integração lenta"), "EasifyAI",
                NivelInteresse.ALTO, RiscoChurn.MEDIO, List.of("Concorrente A", "Concorrente B"),
                "Enviar proposta", "Cliente avaliando a renovação.");
    }

    @Test
    void insereBuscaEReabreArquivoPreservandoTodosOsDados() {
        var original = exemplo();
        dao.inserir(original, TRANSCRICAO);

        // Nenhuma conexão fica aberta entre operações; outra fábrica reabre o arquivo persistido.
        var outraConexao = novaConexao();

        var recuperada = new AnaliseDAO(outraConexao).buscarPorId(original.getId()).orElseThrow();

        assertThat(recuperada).usingRecursiveComparison().isEqualTo(original);
        assertThat(recuperada.exigeAtencaoImediata()).isTrue();
        assertThat(recuperada.descreverConversa()).isEqualTo(original.descreverConversa());
        assertThat(recuperada.identificarPendenciasComerciais()).isEqualTo(original.identificarPendenciasComerciais());
        assertThat(recuperada.podeEnviarProposta()).isEqualTo(original.podeEnviarProposta());
        assertThat(recuperada.classificarPrioridadeComercial()).isEqualTo(original.classificarPrioridadeComercial());
        assertThat(recuperada.sugerirAcaoComercial()).isEqualTo(original.sugerirAcaoComercial());
    }

    @Test
    void listaVaziaEDuasAnalisesComIdentidadesDiferentes() {
        assertThat(dao.listar()).isEmpty();
        var primeira = exemplo();
        var segunda = exemplo();
        dao.inserir(primeira, TRANSCRICAO);
        dao.inserir(segunda, TRANSCRICAO);

        assertThat(dao.listar()).extracting(AnaliseTranscricao::getId)
                .containsExactlyInAnyOrder(primeira.getId(), segunda.getId());
    }

    @Test
    void atualizaCamposEColecoesSemMudarIdentidadeOuOutroRegistro() {
        var original = exemplo();
        var outra = exemplo();
        dao.inserir(original, TRANSCRICAO);
        dao.inserir(outra, TRANSCRICAO);
        var corrigida = new AnaliseTranscricao(original.getId(), new Cliente("Carla", "Nova empresa"),
                new Vendedor("Daniel"), List.of("Nova dor"), "Outro produto", NivelInteresse.BAIXO,
                RiscoChurn.ALTO, List.of(), "Agendar reunião", "Resumo corrigido", original.getDataCriacao());

        assertThat(dao.atualizar(corrigida)).isTrue();
        assertThat(dao.buscarPorId(original.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(corrigida);
        assertThat(dao.buscarPorId(outra.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(outra);
    }

    @Test
    void excluiAnaliseEColecoesSemAfetarOutraAnalise() throws Exception {
        var original = exemplo();
        var outra = exemplo();
        dao.inserir(original, TRANSCRICAO);
        dao.inserir(outra, TRANSCRICAO);

        long idBanco;
        try (var conexao = banco.abrir(); var statement = conexao.prepareStatement(
                "SELECT ID FROM ANALISE WHERE UUID_ANALISE = ?")) {
            statement.setString(1, original.getId().toString());
            try (var resultado = statement.executeQuery()) {
                resultado.next();
                idBanco = resultado.getLong(1);
            }
        }
        dao.registrarStatus(original.getId(), com.totvs.easifyai.domain.StatusAcompanhamento.PENDENTE, null);
        assertThat(dao.excluir(original.getId())).isTrue();
        assertThat(dao.buscarPorId(original.getId())).isEmpty();
        assertThat(dao.buscarPorId(outra.getId())).isPresent();
        try (var conexao = banco.abrir()) {
            for (String tabela : List.of("analise_dor", "analise_concorrente", "transcricao", "status_acompanhamento")) {
                try (var statement = conexao.prepareStatement(
                        "SELECT COUNT(*) FROM " + tabela + " WHERE analise_id = ?")) {
                    statement.setLong(1, idBanco);
                    try (var resultado = statement.executeQuery()) {
                        resultado.next();
                        assertThat(resultado.getInt(1)).isZero();
                    }
                }
            }
        }
    }

    @Test
    void idInexistenteNaoEhCriadoPorAtualizacaoOuExclusao() {
        assertThat(dao.buscarPorId(UUID.randomUUID())).isEmpty();
        assertThat(dao.atualizar(exemplo())).isFalse();
        assertThat(dao.excluir(UUID.randomUUID())).isFalse();
        assertThat(dao.listar()).isEmpty();
    }

    @Test
    void idDuplicadoNaoSobrescreveAnaliseExistente() {
        var original = exemplo();
        dao.inserir(original, TRANSCRICAO);

        assertThatThrownBy(() -> dao.inserir(original, TRANSCRICAO)).isInstanceOf(PersistenciaException.class);
        assertThat(dao.buscarPorId(original.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(original);
        assertThat(dao.listar()).hasSize(1);
    }

    @Test
    void textoComSqlEhArmazenadoComoDado() {
        var analise = new AnaliseTranscricao(new Cliente("Ana'); DROP TABLE analise; --", "ACME"),
                new Vendedor("Bruno"), null, null, NivelInteresse.NAO_IDENTIFICADO,
                RiscoChurn.NAO_IDENTIFICADO, null, null, null);
        dao.inserir(analise, TRANSCRICAO);

        assertThat(dao.buscarPorId(analise.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(analise);
        assertThat(dao.listar()).hasSize(1);
    }

    @Test
    void falhaNaColecaoDesfazInsercaoDoRegistroPrincipal() throws Exception {
        limitarConcorrentes();
        var analise = exemplo();

        assertThatThrownBy(() -> dao.inserir(analise, TRANSCRICAO)).isInstanceOf(PersistenciaException.class);
        assertThat(dao.buscarPorId(analise.getId())).isEmpty();
        try (var conexao = banco.abrir(); var statement = conexao.createStatement();
             var resultado = statement.executeQuery("SELECT COUNT(*) FROM analise_dor")) {
            resultado.next();
            assertThat(resultado.getInt(1)).isZero();
        }
    }

    @Test
    void falhaNaColecaoDesfazAtualizacaoERestauraItensAnteriores() throws Exception {
        var original = new AnaliseTranscricao(new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                List.of("Dor original"), "Produto original", NivelInteresse.BAIXO, RiscoChurn.BAIXO,
                List.of("A"), "Ação original", "Resumo original");
        dao.inserir(original, TRANSCRICAO);
        limitarConcorrentes();
        var corrigida = new AnaliseTranscricao(original.getId(), original.getCliente(), original.getVendedor(),
                List.of("Dor nova"), "Produto novo", NivelInteresse.ALTO, RiscoChurn.ALTO,
                List.of("A", "B"), "Ação nova", "Resumo novo");

        assertThatThrownBy(() -> dao.atualizar(corrigida)).isInstanceOf(PersistenciaException.class);
        assertThat(dao.buscarPorId(original.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void armazenaTranscricaoOriginalEHistoricoSemSobrescreverEventos() {
        var original = exemplo();
        dao.inserir(original, TRANSCRICAO);
        assertThat(dao.buscarTranscricao(original.getId())).contains(TRANSCRICAO);
        assertThat(dao.listarHistorico(original.getId())).isEmpty();
        assertThat(dao.registrarStatus(original.getId(),
                com.totvs.easifyai.domain.StatusAcompanhamento.PENDENTE, null)).isTrue();
        assertThat(dao.registrarStatus(original.getId(),
                com.totvs.easifyai.domain.StatusAcompanhamento.EM_ANDAMENTO, "Contato agendado")).isTrue();
        var historico = dao.listarHistorico(original.getId());
        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).status()).isEqualTo(com.totvs.easifyai.domain.StatusAcompanhamento.PENDENTE);
        assertThat(historico.get(0).observacao()).isNull();
        assertThat(historico.get(1).observacao()).isEqualTo("Contato agendado");
        assertThat(historico.get(1).dataMudanca()).isNotNull();
        assertThat(dao.atualizar(original)).isTrue();
        assertThat(dao.buscarTranscricao(original.getId())).contains(TRANSCRICAO);
        assertThat(dao.listarHistorico(original.getId())).isEqualTo(historico);
        var ausente = UUID.randomUUID();
        assertThat(dao.buscarTranscricao(ausente)).isEmpty();
        assertThat(dao.listarHistorico(ausente)).isEmpty();
        assertThat(dao.registrarStatus(ausente,
                com.totvs.easifyai.domain.StatusAcompanhamento.PENDENTE, null)).isFalse();
    }

    @Test
    void falhaNaTranscricaoDesfazAnaliseEColecoes() throws Exception {
        try (var conexao = banco.abrir(); var statement = conexao.createStatement()) {
            statement.execute("ALTER TABLE TRANSCRICAO ADD CONSTRAINT falha_teste CHECK (ANALISE_ID < 0)");
        }
        var original = exemplo();
        assertThatThrownBy(() -> dao.inserir(original, TRANSCRICAO)).isInstanceOf(PersistenciaException.class);
        assertThat(dao.listar()).isEmpty();
        for (String tabela : List.of("ANALISE_DOR", "ANALISE_CONCORRENTE", "TRANSCRICAO")) {
            try (var conexao = banco.abrir(); var statement = conexao.createStatement();
                 var resultado = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
                resultado.next();
                assertThat(resultado.getInt(1)).isZero();
            }
        }
    }

    @Test
    void transcricaoUnicaPorAnaliseEChavesEstrangeirasSaoObrigatorias() throws Exception {
        var original = exemplo();
        dao.inserir(original, TRANSCRICAO);
        try (var conexao = banco.abrir(); var statement = conexao.createStatement()) {
            assertThatThrownBy(() -> statement.executeUpdate("""
                    INSERT INTO TRANSCRICAO (ANALISE_ID, TEXTO_ORIGINAL)
                    SELECT ID, 'Duplicada' FROM ANALISE
                    """)).isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> statement.executeUpdate("""
                    INSERT INTO ANALISE_DOR (ANALISE_ID, POSICAO, DESCRICAO) VALUES (-1, 0, 'Órfã')
                    """)).isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> statement.executeUpdate("""
                    INSERT INTO STATUS_ACOMPANHAMENTO (ANALISE_ID, STATUS)
                    SELECT ID, 'INVALIDO' FROM ANALISE
                    """)).isInstanceOf(java.sql.SQLException.class);
        }
    }

    private void limitarConcorrentes() throws Exception {
        // Restrição exclusiva do teste provoca falha SQL depois de escrever o pai e as dores.
        try (var conexao = banco.abrir(); var statement = conexao.createStatement()) {
            statement.execute("ALTER TABLE analise_concorrente ADD CONSTRAINT limite_teste CHECK (posicao < 1)");
        }
    }
}
