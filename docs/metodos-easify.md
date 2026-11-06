# Quatro métodos de negócio do EasifyAI
 
Estas regras constituem a política comercial proposta para esta versão do projeto.
Não foram fornecidas pelo professor nem representam um modelo estatístico validado.
São determinísticas e podem ser explicadas e testadas sem executar a Gemini.
 
Todos os métodos estão em `domain/AnaliseTranscricao.java`: a análise já possui
os dados necessários, portanto a decisão pertence ao domínio. Os três enums
novos tipam pendências, prioridade e ação; não representam serviços ou tabelas.
 
## 1. identificarPendenciasComerciais()
 
Retorna todas as informações faltantes para qualificar uma proposta:
 
| Condição | Pendência |
|---|---|
| Interesse desconhecido | `INTERESSE_NAO_IDENTIFICADO` |
| Risco desconhecido | `RISCO_NAO_IDENTIFICADO` |
| Nenhuma dor identificada | `DORES_NAO_IDENTIFICADAS` |
| Nenhum produto recomendado | `PRODUTO_NAO_RECOMENDADO` |
 
O resultado é uma lista imutável. Informações desconhecidas não são consideradas
valores baixos. Falta de dores não significa erro da transcrição; significa que
ainda não há necessidade comercial identificada para justificar uma proposta.
O construtor já normaliza textos e remove itens vazios das listas.
 
## 2. podeEnviarProposta()
 
Uma proposta está qualificada somente quando as quatro condições são satisfeitas:
 
- Interesse alto.
- Risco de churn baixo e conhecido.
- Pelo menos uma dor identificada.
- Produto recomendado preenchido.
 
Concorrentes não tornam a oportunidade inelegível, mas mudam a preparação indicada
para a proposta. O método não envia mensagens nem garante que o produto realmente
resolve a dor: essa adequação continua dependendo da análise e da revisão comercial.
 
## 3. classificarPrioridadeComercial()
 
As condições são avaliadas nesta ordem, retornando a primeira prioridade aplicável:
 
| Prioridade | Condição |
|---|---|
| `IMEDIATA` | Risco alto, ou interesse alto com concorrentes; reutiliza a regra anterior `exigeAtencaoImediata()`. |
| `ALTA` | Interesse alto, ou risco médio com concorrentes. |
| `MEDIA` | Risco médio, interesse médio ou qualquer pendência de diagnóstico. |
| `BAIXA` | Demais situações: interesse e risco baixos, sem pendências. |
 
Sinais conhecidos de urgência prevalecem sobre dados faltantes. A prioridade
orienta a ordem de atendimento, sem estabelecer horas, dias ou SLA.
 
## 4. sugerirAcaoComercial()
 
A primeira condição satisfeita determina a ação:
 
| Ordem | Condição | Ação |
|---|---|---|
| 1 | Risco alto | `PLANEJAR_RETENCAO` |
| 2 | Alguma pendência comercial | `COMPLETAR_DIAGNOSTICO` |
| 3 | Risco médio | `TRATAR_OBJECOES` |
| 4 | Interesse alto e concorrentes | `DIFERENCIAR_PROPOSTA` |
| 5 | Proposta qualificada | `ENVIAR_PROPOSTA` |
| 6 | Interesse médio | `APRESENTAR_SOLUCAO` |
| 7 | Demais situações | `NUTRIR_RELACIONAMENTO` |
 
Não é necessário completar todos os dados para reconhecer um risco alto já
identificado. Nas demais situações, a coleta de informações antecede a proposta.
Depois do diagnóstico, risco médio exige tratar objeções antes de avançar.
Uma oportunidade qualificada com concorrentes requer diferenciação antes do envio.
 
## Uso real e contrato HTTP
 
`AnaliseResponse.de()` e `MainTerminal` utilizam os quatro métodos. O endpoint
`POST /analisar` acrescenta estes campos à resposta, preservando os anteriores:
 
```json
{
  "pendenciasComerciais": [],
  "podeEnviarProposta": true,
  "prioridadeComercial": "IMEDIATA",
  "acaoComercialSugerida": "DIFERENCIAR_PROPOSTA"
}
```
 
Esse exemplo corresponde a interesse alto, risco baixo, dor e produto identificados
e presença de concorrentes. `proximaAcao` continua contendo a sugestão textual da
Gemini; `acaoComercialSugerida` é a decisão calculada pelo Java. As saídas são sugestões
para revisão humana, sem execução automática de uma ação comercial.
 
Os resultados são derivados dos dados da análise e não precisam ser gravados em
novas colunas. Ao ler uma análise do banco, o Java os recalcula segundo a política
atual; este escopo não registra o histórico de versões da política comercial.
 
## Testes e apresentação acadêmica
 
`RegrasComerciaisTest` instancia clientes, vendedores e análises e testa os quatro
métodos, incluindo risco desconhecido, campos ausentes, normalização e precedência.
`AnaliseControllerTest` verifica os novos campos HTTP. `AnaliseDAOTest` verifica que
as decisões são mantidas após gravar e recuperar a análise.
 
Para apresentar, use três exemplos: oportunidade qualificada sem concorrentes
(enviar proposta), risco alto mesmo com dados ausentes (retenção) e risco desconhecido
(completar diagnóstico). Explique a diferença entre qualificação, urgência e ação.
Os quatro métodos têm responsabilidades distintas; os métodos CRUD não são contados
como regras comerciais para este critério.