package com.totvs.easifyai.infrastructure.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Modelo da resposta que pedimos à Gemini (o JSON descrito no prompt).
 *
 * <p>É intencionalmente separado do domínio: o formato devolvido pela IA não
 * deve ditar a estrutura interna do EasifyAI. A conversão para o domínio é
 * feita pelo {@link GeminiRespostaMapper}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiAnaliseResponse {

    private String cliente;
    private String empresaCliente;
    private String vendedor;
    private List<String> dores;
    private String produtoRecomendado;
    private String nivelInteresse;
    private String riscoChurn;
    private List<String> concorrentes;
    private String proximaAcao;
    private String resumo;

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEmpresaCliente() {
        return empresaCliente;
    }

    public void setEmpresaCliente(String empresaCliente) {
        this.empresaCliente = empresaCliente;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public List<String> getDores() {
        return dores;
    }

    public void setDores(List<String> dores) {
        this.dores = dores;
    }

    public String getProdutoRecomendado() {
        return produtoRecomendado;
    }

    public void setProdutoRecomendado(String produtoRecomendado) {
        this.produtoRecomendado = produtoRecomendado;
    }

    public String getNivelInteresse() {
        return nivelInteresse;
    }

    public void setNivelInteresse(String nivelInteresse) {
        this.nivelInteresse = nivelInteresse;
    }

    public String getRiscoChurn() {
        return riscoChurn;
    }

    public void setRiscoChurn(String riscoChurn) {
        this.riscoChurn = riscoChurn;
    }

    public List<String> getConcorrentes() {
        return concorrentes;
    }

    public void setConcorrentes(List<String> concorrentes) {
        this.concorrentes = concorrentes;
    }

    public String getProximaAcao() {
        return proximaAcao;
    }

    public void setProximaAcao(String proximaAcao) {
        this.proximaAcao = proximaAcao;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }
}
