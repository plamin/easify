package com.totvs.easifyai.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Resultado da análise de uma transcrição comercial.
 *
 * <p>É a entidade central do domínio do EasifyAI. Em vez de um saco de getters
 * e setters, guarda participantes reais ({@link Cliente} e {@link Vendedor}),
 * objetos de valor ({@link NivelInteresse}, {@link RiscoChurn}) e concentra as
 * regras que a equipe comercial usa para decidir a próxima ação.
 *
 * <p>É imutável: correções são representadas por outra instância com o mesmo ID.
 */
public class AnaliseTranscricao {

    private final UUID id;
    private final LocalDateTime dataCriacao;
    private final Cliente cliente;
    private final Vendedor vendedor;
    private final List<String> dores;
    private final String produtoRecomendado;
    private final NivelInteresse nivelInteresse;
    private final RiscoChurn riscoChurn;
    private final List<String> concorrentes;
    private final String proximaAcao;
    private final String resumo;

    public AnaliseTranscricao(Cliente cliente,
                              Vendedor vendedor,
                              List<String> dores,
                              String produtoRecomendado,
                              NivelInteresse nivelInteresse,
                              RiscoChurn riscoChurn,
                              List<String> concorrentes,
                              String proximaAcao,
                              String resumo) {
        this(UUID.randomUUID(), cliente, vendedor, dores, produtoRecomendado,
                nivelInteresse, riscoChurn, concorrentes, proximaAcao, resumo);
    }

    /** Reconstitui uma análise existente, preservando sua identidade. */
    public AnaliseTranscricao(UUID id,
                              Cliente cliente,
                              Vendedor vendedor,
                              List<String> dores,
                              String produtoRecomendado,
                              NivelInteresse nivelInteresse,
                              RiscoChurn riscoChurn,
                              List<String> concorrentes,
                              String proximaAcao,
                              String resumo) {
        this(id, cliente, vendedor, dores, produtoRecomendado, nivelInteresse, riscoChurn,
                concorrentes, proximaAcao, resumo, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    /** Reconstrói a entidade com a data persistida, sem alterar sua identidade. */
    public AnaliseTranscricao(UUID id, Cliente cliente, Vendedor vendedor, List<String> dores,
                              String produtoRecomendado, NivelInteresse nivelInteresse,
                              RiscoChurn riscoChurn, List<String> concorrentes,
                              String proximaAcao, String resumo, LocalDateTime dataCriacao) {
        this.dataCriacao = exigir(dataCriacao, "data de criação");
        this.id = exigir(id, "id");
        this.cliente = exigir(cliente, "cliente");
        this.vendedor = exigir(vendedor, "vendedor");
        this.nivelInteresse = exigir(nivelInteresse, "nível de interesse");
        this.riscoChurn = exigir(riscoChurn, "risco de churn");
        this.dores = copiaImutavel(dores);
        this.concorrentes = copiaImutavel(concorrentes);
        this.produtoRecomendado = textoOuVazio(produtoRecomendado);
        this.proximaAcao = textoOuVazio(proximaAcao);
        this.resumo = textoOuVazio(resumo);
    }

    /**
     * Participantes da conversa. A ordem reflete o fluxo do atendimento
     * (vendedor conduz, cliente responde).
     */
    public List<Pessoa> participantes() {
        return List.of(vendedor, cliente);
    }

    /**
     * Descrição da conversa montada a partir do papel de cada participante.
     * Percorre {@link #participantes()} chamando {@link Pessoa#apresentar()},
     * que resolve {@code descreverPapel()} de forma polimórfica.
     */
    public String descreverConversa() {
        return participantes().stream()
                .map(Pessoa::apresentar)
                .reduce((a, b) -> a + " e " + b)
                .orElse("");
    }

    /**
     * Regra de negócio: a conta precisa de acompanhamento imediato quando o
     * risco de churn é crítico, ou quando há concorrentes na mesa mesmo com o
     * cliente demonstrando interesse alto.
     */
    public boolean exigeAtencaoImediata() {
        return riscoChurn.isCritico()
                || (nivelInteresse.isAlto() && mencionaConcorrentes());
    }

    public boolean mencionaConcorrentes() {
        return !concorrentes.isEmpty();
    }

    public boolean possuiRiscoElevado() {
        return riscoChurn.isCritico();
    }

    /** Acumula todas as lacunas; desconhecido nunca equivale a risco ou interesse baixo. */
    public List<PendenciaComercial> identificarPendenciasComerciais() {
        List<PendenciaComercial> pendencias = new ArrayList<>();
        if (nivelInteresse == NivelInteresse.NAO_IDENTIFICADO) {
            pendencias.add(PendenciaComercial.INTERESSE_NAO_IDENTIFICADO);
        }
        if (riscoChurn == RiscoChurn.NAO_IDENTIFICADO) {
            pendencias.add(PendenciaComercial.RISCO_NAO_IDENTIFICADO);
        }
        if (dores.isEmpty()) {
            pendencias.add(PendenciaComercial.DORES_NAO_IDENTIFICADAS);
        }
        if (produtoRecomendado.isBlank()) {
            pendencias.add(PendenciaComercial.PRODUTO_NAO_RECOMENDADO);
        }
        return List.copyOf(pendencias);
    }

    /** Qualifica a proposta; a existência de concorrentes exige diferenciação, não bloqueio. */
    public boolean podeEnviarProposta() {
        return nivelInteresse == NivelInteresse.ALTO
                && riscoChurn == RiscoChurn.BAIXO
                && !dores.isEmpty()
                && !produtoRecomendado.isBlank();
    }

    /** Prioriza primeiro sinais conhecidos de urgência e depois oportunidade e diagnóstico. */
    public PrioridadeComercial classificarPrioridadeComercial() {
        if (exigeAtencaoImediata()) {
            return PrioridadeComercial.IMEDIATA;
        }
        if (nivelInteresse == NivelInteresse.ALTO
                || (riscoChurn == RiscoChurn.MEDIO && mencionaConcorrentes())) {
            return PrioridadeComercial.ALTA;
        }
        if (riscoChurn == RiscoChurn.MEDIO || nivelInteresse == NivelInteresse.MEDIO
                || !identificarPendenciasComerciais().isEmpty()) {
            return PrioridadeComercial.MEDIA;
        }
        return PrioridadeComercial.BAIXA;
    }

    /**
     * Escolhe a ação pela precedência: retenção, diagnóstico, objeções, diferenciação,
     * proposta, apresentação e nutrição. Não executa nem envia nada ao cliente.
     */
    public AcaoComercial sugerirAcaoComercial() {
        if (riscoChurn == RiscoChurn.ALTO) {
            return AcaoComercial.PLANEJAR_RETENCAO;
        }
        if (!identificarPendenciasComerciais().isEmpty()) {
            return AcaoComercial.COMPLETAR_DIAGNOSTICO;
        }
        if (riscoChurn == RiscoChurn.MEDIO) {
            return AcaoComercial.TRATAR_OBJECOES;
        }
        if (nivelInteresse == NivelInteresse.ALTO && mencionaConcorrentes()) {
            return AcaoComercial.DIFERENCIAR_PROPOSTA;
        }
        if (podeEnviarProposta()) {
            return AcaoComercial.ENVIAR_PROPOSTA;
        }
        if (nivelInteresse == NivelInteresse.MEDIO) {
            return AcaoComercial.APRESENTAR_SOLUCAO;
        }
        return AcaoComercial.NUTRIR_RELACIONAMENTO;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public UUID getId() {
        return id;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public List<String> getDores() {
        return dores;
    }

    public String getProdutoRecomendado() {
        return produtoRecomendado;
    }

    public NivelInteresse getNivelInteresse() {
        return nivelInteresse;
    }

    public RiscoChurn getRiscoChurn() {
        return riscoChurn;
    }

    public List<String> getConcorrentes() {
        return concorrentes;
    }

    public String getProximaAcao() {
        return proximaAcao;
    }

    public String getResumo() {
        return resumo;
    }

    private static <T> T exigir(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Análise inválida: " + campo + " é obrigatório.");
        }
        return valor;
    }

    private static List<String> copiaImutavel(List<String> origem) {
        if (origem == null) {
            return List.of();
        }
        return origem.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .toList();
    }

    private static String textoOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
