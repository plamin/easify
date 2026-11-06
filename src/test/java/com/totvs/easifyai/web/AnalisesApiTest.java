package com.totvs.easifyai.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.application.GerenciarAnalisesService;
import com.totvs.easifyai.domain.*;
import com.totvs.easifyai.infrastructure.persistence.AnaliseDAO;
import com.totvs.easifyai.infrastructure.persistence.BancoTeste;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalisesApiTest {
    @TempDir Path diretorio;
    private AnaliseDAO dao;
    private MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper();
    private static final String TRANSCRICAO = "  Conversa com o cliente sobre a renovação do contrato.  ";
    private static final String EDICAO = """
            {"cliente":"Ana","empresaCliente":"ACME","vendedor":"Bruno",
             "dores":["Integração lenta"],"produtoRecomendado":"EasifyAI",
             "nivelInteresse":"ALTO","riscoChurn":"BAIXO","concorrentes":[],
             "proximaAcao":"Enviar proposta revisada","resumo":"Resumo corrigido"}
            """;

    @BeforeEach
    void preparar() {
        dao = new AnaliseDAO(BancoTeste.criar(diretorio.resolve("api")));
        Analisador fake = texto -> exemplo();
        var casoDeUso = new AnalisarTranscricaoUseCase(fake, dao);
        mvc = MockMvcBuilders.standaloneSetup(new AnalisesController(casoDeUso, new GerenciarAnalisesService(dao)),
                        new AnaliseController(casoDeUso))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private AnaliseTranscricao exemplo() {
        return new AnaliseTranscricao(new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                List.of("Preço"), "EasifyAI", NivelInteresse.MEDIO, RiscoChurn.BAIXO,
                List.of(), "Agendar reunião", "Resumo");
    }

    private String criar() throws Exception {
        var result = mvc.perform(post("/analises").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("transcricao", TRANSCRICAO))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.dataCriacao").isString()).andReturn();
        String id = json.readTree(result.getResponse().getContentAsString()).get("id").asText();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("/analises/" + id);
        return id;
    }

    @Test
    void cicloCompletoPreservaOriginaisEExcluiDependentes() throws Exception {
        mvc.perform(get("/analises")).andExpect(status().isOk()).andExpect(content().json("[]"));
        String id = criar();
        var original = dao.buscarPorId(UUID.fromString(id)).orElseThrow();
        mvc.perform(get("/analises")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/analises/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.cliente").value("Ana"));
        mvc.perform(get("/analises/{id}/transcricao", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.textoOriginal").value(TRANSCRICAO));
        mvc.perform(get("/analises/{id}/acompanhamentos", id)).andExpect(content().json("[]"));
        mvc.perform(post("/analises/{id}/acompanhamentos", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_ANDAMENTO\",\"observacao\":\"Contato realizado\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/analises/{id}", id).contentType(MediaType.APPLICATION_JSON).content(EDICAO))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.podeEnviarProposta").value(true));
        assertThat(dao.buscarPorId(original.getId()).orElseThrow().getDataCriacao()).isEqualTo(original.getDataCriacao());
        assertThat(dao.buscarTranscricao(original.getId())).contains(TRANSCRICAO);
        mvc.perform(get("/analises/{id}/acompanhamentos", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$[0].dataMudanca").isString());
        mvc.perform(delete("/analises/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/analises/{id}", id)).andExpect(status().isNotFound());
        assertThat(dao.buscarTranscricao(original.getId())).isEmpty();
        assertThat(dao.listarHistorico(original.getId())).isEmpty();
    }

    @Test
    void analiseInexistenteNaoEhConfundidaComHistoricoVazio() throws Exception {
        String id = UUID.randomUUID().toString();
        for (String sufixo : List.of("", "/transcricao", "/acompanhamentos")) {
            mvc.perform(get("/analises/" + id + sufixo)).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.erro").value("analise_nao_encontrada"));
        }
        mvc.perform(put("/analises/{id}", id).contentType(MediaType.APPLICATION_JSON).content(EDICAO))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/analises/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(post("/analises/{id}/acompanhamentos", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PENDENTE\"}")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "{}", "{\"cliente\":\"Ana\"}"})
    void atualizacaoInvalidaNaoMudaDados(String corpo) throws Exception {
        String id = criar();
        var antes = dao.buscarPorId(UUID.fromString(id)).orElseThrow();
        mvc.perform(put("/analises/{id}", id).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isString());
        assertThat(dao.buscarPorId(antes.getId()).orElseThrow()).usingRecursiveComparison().isEqualTo(antes);
    }

    @Test
    void validaEnumsItensEBytesAntesDeAtualizar() throws Exception {
        String id = criar();
        for (String corpo : List.of(EDICAO.replace("\"ALTO\"", "\"INVALIDO\""),
                EDICAO.replace("\"Ana\"", "\"" + "é".repeat(251) + "\""),
                EDICAO.replace("[\"Integração lenta\"]", "[null]"),
                EDICAO.replace("\"Bruno\"", "\"  \""))) {
            mvc.perform(put("/analises/{id}", id).contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/analises/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(EDICAO.replace("\"Ana\"", "\"" + "é".repeat(250) + "\"")))
                .andExpect(status().isOk());
    }

    @Test
    void validaStatusEObservacaoAcumulandoEventos() throws Exception {
        String id = criar();
        for (String corpo : List.of("{}", "{\"status\":\"INVALIDO\"}",
                "{\"status\":\"PENDENTE\",\"observacao\":\"" + "é".repeat(501) + "\"}")) {
            mvc.perform(post("/analises/{id}/acompanhamentos", id).contentType(MediaType.APPLICATION_JSON)
                    .content(corpo)).andExpect(status().isBadRequest());
        }
        assertThat(dao.listarHistorico(UUID.fromString(id))).isEmpty();
        for (String status : List.of("PENDENTE", "EM_ANDAMENTO", "CONCLUIDO", "DESCARTADO")) {
            mvc.perform(post("/analises/{id}/acompanhamentos", id).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"" + status + "\",\"observacao\":\"" + "é".repeat(500) + "\"}"))
                    .andExpect(status().isNoContent());
        }
        assertThat(dao.listarHistorico(UUID.fromString(id))).hasSize(4);
    }

    @Test
    void errosDeProtocoloPreservamStatusHttp() throws Exception {
        mvc.perform(get("/analises/nao-e-uuid")).andExpect(status().isBadRequest());
        mvc.perform(patch("/analises/{id}", UUID.randomUUID())).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/analises").contentType(MediaType.TEXT_PLAIN).content("teste"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(post("/analises").contentType(MediaType.APPLICATION_JSON).content("{\"transcricao\":\"oi\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("transcricao_invalida"));
    }
}
