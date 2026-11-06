package com.totvs.easifyai.infrastructure.persistence;

import com.totvs.easifyai.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: utiliza o schema existente e remove somente o registro criado pelo teste. */
@EnabledIfSystemProperty(named = "easify.testar-oracle", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OracleIntegracaoTest {
    @Autowired AnaliseRepository repository;

    @Test
    void cicloCompletoNoOracle() {
        var analise = new AnaliseTranscricao(new Cliente("Teste integração", "Empresa fictícia"),
                new Vendedor("Teste automático"), List.of("Processo manual"), "CRM",
                NivelInteresse.ALTO, RiscoChurn.BAIXO, List.of("Concorrente fictício"),
                "Enviar proposta", "Registro temporário de validação");
        boolean inserido = false;
        try {
            repository.inserir(analise, "Transcrição fictícia de integração Oracle.");
            inserido = true;
            var lida = repository.buscarPorId(analise.getId()).orElseThrow();
            assertEquals(analise.getDataCriacao(), lida.getDataCriacao());
            assertEquals(analise.getDores(), lida.getDores());
            assertEquals(analise.getConcorrentes(), lida.getConcorrentes());
            assertTrue(repository.listar().stream().anyMatch(a -> a.getId().equals(analise.getId())));
            var corrigida = new AnaliseTranscricao(analise.getId(), analise.getCliente(),
                    analise.getVendedor(), List.of("Dor corrigida"), "ERP", NivelInteresse.MEDIO,
                    RiscoChurn.ALTO, List.of(), "Agendar reunião", "Resumo corrigido", analise.getDataCriacao());
            assertTrue(repository.atualizar(corrigida));
            assertEquals("ERP", repository.buscarPorId(analise.getId()).orElseThrow().getProdutoRecomendado());
            assertTrue(repository.registrarStatus(analise.getId(), StatusAcompanhamento.EM_ANDAMENTO, "Teste"));
            assertEquals(1, repository.listarHistorico(analise.getId()).size());
            assertEquals("Transcrição fictícia de integração Oracle.", repository.buscarTranscricao(analise.getId()).orElseThrow());
            assertTrue(repository.excluir(analise.getId()));
            assertTrue(repository.buscarPorId(analise.getId()).isEmpty());
            assertTrue(repository.buscarTranscricao(analise.getId()).isEmpty());
            assertTrue(repository.listarHistorico(analise.getId()).isEmpty());
        } finally {
            if (inserido) repository.excluir(analise.getId());
        }
    }
}
