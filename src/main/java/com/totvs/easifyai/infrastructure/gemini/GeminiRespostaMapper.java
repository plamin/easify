package com.totvs.easifyai.infrastructure.gemini;

import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.Cliente;
import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import com.totvs.easifyai.domain.Vendedor;
import org.springframework.stereotype.Component;

/**
 * Traduz a resposta da Gemini ({@link GeminiAnaliseResponse}) para o modelo do
 * domínio ({@link AnaliseTranscricao}), montando os participantes e convertendo
 * os textos livres da IA em objetos de valor.
 */
@Component
public class GeminiRespostaMapper {

    private static final String NOME_NAO_INFORMADO = "Não informado";

    public AnaliseTranscricao paraDominio(GeminiAnaliseResponse resposta) {
        Cliente cliente = new Cliente(
                nomeOuPadrao(resposta.getCliente()),
                resposta.getEmpresaCliente());

        Vendedor vendedor = new Vendedor(nomeOuPadrao(resposta.getVendedor()));

        return new AnaliseTranscricao(
                cliente,
                vendedor,
                resposta.getDores(),
                resposta.getProdutoRecomendado(),
                NivelInteresse.fromTexto(resposta.getNivelInteresse()),
                RiscoChurn.fromTexto(resposta.getRiscoChurn()),
                resposta.getConcorrentes(),
                resposta.getProximaAcao(),
                resposta.getResumo());
    }

    private static String nomeOuPadrao(String nome) {
        return (nome == null || nome.isBlank()) ? NOME_NAO_INFORMADO : nome.trim();
    }
}
