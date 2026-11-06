package com.totvs.easifyai.web;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.Cliente;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import com.totvs.easifyai.domain.Vendedor;
import com.totvs.easifyai.infrastructure.persistence.AnaliseDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnaliseControllerTest {
    @Test
    void respostaIncluiIdDaAnaliseEfetivamentePersistida(@TempDir Path diretorio) throws Exception {
        var banco = com.totvs.easifyai.infrastructure.persistence.BancoTeste.criar(diretorio.resolve("api"));
        var dao = new AnaliseDAO(banco);
        var analise = new AnaliseTranscricao(new Cliente("Ana", "ACME"), new Vendedor("Bruno"),
                List.of("Preço"), "EasifyAI", NivelInteresse.ALTO, RiscoChurn.ALTO,
                List.of(), "Agendar reunião", "Resumo");
        var casoDeUso = new AnalisarTranscricaoUseCase(texto -> analise, dao);
        var mvc = MockMvcBuilders.standaloneSetup(new AnaliseController(casoDeUso))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/analisar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transcricao\":\"Reunião comercial para renovação de contrato\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(analise.getId().toString()))
                .andExpect(jsonPath("$.cliente").value("Ana"))
                .andExpect(jsonPath("$.exigeAtencaoImediata").value(true))
                .andExpect(jsonPath("$.pendenciasComerciais").isEmpty())
                .andExpect(jsonPath("$.podeEnviarProposta").value(false))
                .andExpect(jsonPath("$.prioridadeComercial").value("IMEDIATA"))
                .andExpect(jsonPath("$.acaoComercialSugerida").value("PLANEJAR_RETENCAO"));

        assertThat(dao.buscarTranscricao(analise.getId())).contains("Reunião comercial para renovação de contrato");
        assertThat(dao.buscarPorId(analise.getId()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(analise);
    }
}
