package com.totvs.easifyai.web;

import com.totvs.easifyai.domain.AnaliseTranscricao;
import com.totvs.easifyai.domain.AcaoComercial;
import com.totvs.easifyai.domain.PendenciaComercial;
import com.totvs.easifyai.domain.PrioridadeComercial;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Representação da análise no contrato da API REST.
 *
 * <p>Mantém a resposta HTTP estável e independente da modelagem interna: expõe
 * textos simples (não os enums do domínio) e já entrega o indicador de negócio
 * {@code exigeAtencaoImediata} calculado pela entidade.
 */
public record AnaliseResponse(
        String cliente,
        String empresaCliente,
        String vendedor,
        List<String> dores,
        String produtoRecomendado,
        String nivelInteresse,
        String riscoChurn,
        List<String> concorrentes,
        String proximaAcao,
        String resumo,
        boolean exigeAtencaoImediata,
        UUID id,
        List<PendenciaComercial> pendenciasComerciais,
        boolean podeEnviarProposta,
        PrioridadeComercial prioridadeComercial,
        AcaoComercial acaoComercialSugerida,
        LocalDateTime dataCriacao
) {

    public static AnaliseResponse de(AnaliseTranscricao analise) {
        return new AnaliseResponse(
                analise.getCliente().getNome(),
                analise.getCliente().getEmpresa(),
                analise.getVendedor().getNome(),
                analise.getDores(),
                analise.getProdutoRecomendado(),
                analise.getNivelInteresse().name(),
                analise.getRiscoChurn().name(),
                analise.getConcorrentes(),
                analise.getProximaAcao(),
                analise.getResumo(),
                analise.exigeAtencaoImediata(),
                analise.getId(),
                analise.identificarPendenciasComerciais(),
                analise.podeEnviarProposta(),
                analise.classificarPrioridadeComercial(),
                analise.sugerirAcaoComercial(),
                analise.getDataCriacao());
    }
}
