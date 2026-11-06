package com.totvs.easifyai.application;

import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.AnaliseRepository;
import com.totvs.easifyai.domain.Analisador;
import com.totvs.easifyai.domain.TranscricaoInvalidaException;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: analisar uma transcrição comercial.
 *
 * <p>Orquestra o fluxo entre a entrada da aplicação e o domínio: valida a
 * transcrição segundo as regras do negócio, delega a análise para a porta
 * {@link Analisador} e persiste o resultado por {@link AnaliseRepository}.
 * Fica fora do controller para que a camada HTTP não
 * carregue regra de negócio, e depende da abstração (não da Gemini).
 */
@Service
public class AnalisarTranscricaoUseCase {

    /** Abaixo disso o texto não tem conteúdo suficiente para uma análise útil. */
    private static final int TAMANHO_MINIMO = 20;

    private final Analisador analisador;
    private final AnaliseRepository analises;

    public AnalisarTranscricaoUseCase(Analisador analisador, AnaliseRepository analises) {
        this.analisador = analisador;
        this.analises = analises;
    }

    public AnaliseTranscricao executar(String transcricao) {
        String texto = transcricao == null ? "" : transcricao.trim();

        if (texto.isEmpty()) {
            throw new TranscricaoInvalidaException("A transcrição não pode estar vazia.");
        }
        if (texto.length() < TAMANHO_MINIMO) {
            throw new TranscricaoInvalidaException(
                    "A transcrição é curta demais para ser analisada.");
        }

        AnaliseTranscricao analise = analisador.analisarTranscricao(texto);
        try {
            ValidacaoAnalise.validarParticipantes(analise.getCliente().getNome(),
                    analise.getCliente().getEmpresa(), analise.getVendedor().getNome());
        } catch (DadosInvalidosException e) {
            throw new com.totvs.easifyai.domain.AnaliseIndisponivelException(
                    "A análise recebida contém participantes incompatíveis com o cadastro.", e);
        }
        analises.inserir(analise, transcricao);
        return analise;
    }
}
