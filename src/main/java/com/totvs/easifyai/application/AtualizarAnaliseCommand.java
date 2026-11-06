package com.totvs.easifyai.application;

import com.totvs.easifyai.domain.NivelInteresse;
import com.totvs.easifyai.domain.RiscoChurn;
import java.util.List;

/** Dados editáveis. Identidade, datas, transcrição e histórico não fazem parte da alteração. */
public record AtualizarAnaliseCommand(String cliente, String empresaCliente, String vendedor,
        List<String> dores, String produtoRecomendado, NivelInteresse nivelInteresse,
        RiscoChurn riscoChurn, List<String> concorrentes, String proximaAcao, String resumo) { }
