package com.totvs.easifyai.web;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada HTTP para a análise de transcrições.
 *
 * <p>Responsabilidade limitada ao protocolo: recebe a requisição, delega ao
 * caso de uso e devolve o DTO de resposta. Validação de conteúdo e regra de
 * negócio ficam na camada de aplicação e no domínio.
 */
@RestController
public class AnaliseController {

    private final AnalisarTranscricaoUseCase analisarTranscricao;

    public AnaliseController(AnalisarTranscricaoUseCase analisarTranscricao) {
        this.analisarTranscricao = analisarTranscricao;
    }

    @PostMapping("/analisar")
    public AnaliseResponse analisar(@RequestBody AnalisarTranscricaoRequest request) {
        AnaliseTranscricao analise =
                analisarTranscricao.executar(request.getTranscricao());
        return AnaliseResponse.de(analise);
    }
}
