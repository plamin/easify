// Execução da análise direto pelo terminal, útil para testes manuais.
// A entrada oficial da aplicação é a API REST (ver AnaliseController).

package com.totvs.easifyai;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.domain.AnaliseTranscricao;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

public class MainTerminal {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);

        AnalisarTranscricaoUseCase analisarTranscricao =
                context.getBean(AnalisarTranscricaoUseCase.class);

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Transcrição:");
            String transcricao = scanner.nextLine();

            AnaliseTranscricao resultado = analisarTranscricao.executar(transcricao);

            System.out.println();
            System.out.println("=== Análise ===");
            System.out.println("ID salvo no banco: " + resultado.getId());
            System.out.println("Conversa: " + resultado.descreverConversa());
            System.out.println("Dores: " + resultado.getDores());
            System.out.println("Produto recomendado: " + resultado.getProdutoRecomendado());
            System.out.println("Nível de interesse: " + resultado.getNivelInteresse());
            System.out.println("Risco de churn: " + resultado.getRiscoChurn());
            System.out.println("Concorrentes: " + resultado.getConcorrentes());
            System.out.println("Próxima ação: " + resultado.getProximaAcao());
            System.out.println("Resumo: " + resultado.getResumo());
            System.out.println("Exige atenção imediata? " + resultado.exigeAtencaoImediata());
            System.out.println("Pendências comerciais: " + resultado.identificarPendenciasComerciais());
            System.out.println("Pode enviar proposta? " + resultado.podeEnviarProposta());
            System.out.println("Prioridade comercial: " + resultado.classificarPrioridadeComercial());
            System.out.println("Ação calculada pelo sistema: " + resultado.sugerirAcaoComercial());

        } finally {
            context.close();
        }
    }
}
