package com.totvs.easifyai.web;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.application.GerenciarAnalisesService;
import com.totvs.easifyai.domain.AnaliseIndisponivelException;
import com.totvs.easifyai.infrastructure.persistence.PersistenciaException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.sql.SQLException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

class ErrosApiTest {
    @Test
    void falhasTecnicasNaoExponhemDetalhesEConflitosRetornam409() throws Exception {
        var service = mock(GerenciarAnalisesService.class);
        var analisar = mock(AnalisarTranscricaoUseCase.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AnalisesController(analisar, service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        when(service.listar()).thenThrow(new PersistenciaException("SQL_INTERNO",
                new SQLException("SQL_INTERNO", "23000")));
        mvc.perform(get("/analises")).andExpect(status().isConflict())
                .andExpect(content().string(not(containsString("SQL_INTERNO"))));
        doThrow(new PersistenciaException("SQL_INTERNO",
                new SQLException("SQL_INTERNO", "08001"))).when(service).listar();
        mvc.perform(get("/analises")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("erro_interno"));
        when(analisar.executar(any())).thenThrow(new AnaliseIndisponivelException("DETALHE_PROVEDOR"));
        mvc.perform(post("/analises").contentType(MediaType.APPLICATION_JSON)
                .content("{\"transcricao\":\"Transcrição fictícia suficientemente longa\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(not(containsString("DETALHE_PROVEDOR"))));
    }
}
