# EasifyAI — entrega final Java

API para análise de transcrições B2B com Gemini, regras comerciais Java e persistência Oracle.
Java 21, Spring Boot 4.0.6 e Maven. Não há front-end; use a coleção Postman.

## Executar

1. Abra a raiz que contém `pom.xml` na IDE com JDK 21.
2. O Oracle precisa conter as cinco tabelas do projeto. O DDL está em
   `src/main/resources/db/schema.sql`; não execute novamente em um schema já existente.
3. Na cópia distribuída, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `GEMINI_API_KEY`
   nas variáveis de ambiente da execução. A URL pode ser `jdbc:oracle:thin:@//HOST:1521/SERVICE`
   ou a URL com SID fornecida pelo responsável pelo banco. A configuração local já existente
   foi preservada; o ZIP utiliza variáveis e não inclui as credenciais locais.
4. Execute **Main.java** ou `.\mvnw.cmd spring-boot:run`. A API fica em `http://localhost:8080`.
5. Importe `docs/EasifyAI.postman_collection.json` no Postman e execute as requisições em ordem.

Main é a única classe necessária para a API. MainTerminal é uma alternativa de entrada
manual pelo console; não precisa rodar junto. As classes de teste são executadas pelo Maven.
O sistema não cria nem popula tabelas ao iniciar. POST /analises popula o agregado completo.

## Endpoints da API

URL base: `http://localhost:8080`

| Método | Rota | Finalidade | Status de sucesso |
|---|---|---|---|
| POST | `/analises` | Criar uma análise a partir de uma transcrição | 201 Created |
| GET | `/analises` | Listar análises | 200 OK |
| GET | `/analises/{id}` | Consultar uma análise | 200 OK |
| PUT | `/analises/{id}` | Atualizar dados de uma análise | 200 OK |
| DELETE | `/analises/{id}` | Excluir uma análise | 204 No Content |
| GET | `/analises/{id}/transcricao` | Consultar a transcrição original | 200 OK |
| GET | `/analises/{id}/acompanhamentos` | Listar histórico de acompanhamento | 200 OK |
| POST | `/analises/{id}/acompanhamentos` | Registrar status de acompanhamento | 204 No Content |
| POST | `/analisar` | Endpoint alternativo de análise | 200 OK |

Os controllers delegam as operações aos casos de uso e serviços. O tratamento centralizado de exceções está em `GlobalExceptionHandler`, incluindo:

- `400 Bad Request`: transcrição ou dados inválidos.
- `404 Not Found`: análise não encontrada.
- `409 Conflict`: conflito de integridade de dados detectado na persistência.
- `502 Bad Gateway`: indisponibilidade da integração de análise.
- `500 Internal Server Error`: falha inesperada.

Os detalhes de requisições e respostas podem ser testados na [coleção Postman](docs/EasifyAI.postman_collection.json).

## Documentação existente no repositório

- [Documentação final de Java — PDF](docs/doc_java_final.pdf).
- [Documentação complementar — PDF](docs/easifybddoc.pdf).
- [Métodos e regras do EasifyAI](docs/metodos-easify.md).
- [Mapeamento e persistência Oracle](docs/persistencia-oracle.md).
- [Coleção Postman](docs/EasifyAI.postman_collection.json).

> **Observação:** os arquivos `docs/api-rest.md`, `docs/alteracoes-entrega-final.md` e `docs/regras-comerciais.md` eram mencionados na versão anterior do README, mas não estão presentes no pacote recebido. Os links foram removidos para evitar referências quebradas.

## Diagrama e documentação revisada

O diagrama de classes e o PDF revisado da Sprint 4 foram gerados separadamente. Para incluí-los no repositório, salve os arquivos recebidos na pasta `docs/` e acrescente os links correspondentes nesta seção. Não são tratados aqui como arquivos já presentes no projeto.

## Testes e build

```powershell
.\mvnw.cmd verify
```

Os testes padrão usam H2 em modo Oracle e uma IA isolada, sem chamadas Gemini reais.
Para o teste explícito do Oracle configurado:

```powershell
.\mvnw.cmd '-Deasify.testar-oracle=true' '-Dtest=OracleIntegracaoTest' test
```

Esse teste insere um registro fictício com UUID exclusivo, valida o CRUD e remove
somente esse registro em finally. Precisa de banco acessível e schema já criado.

## Feedback 3: conexão acadêmica

`ConexaoBanco` é a fábrica padrão, com configurações externas. `ConexaoAcademica`
é selecionada apenas pelo perfil `academico` e contém usuário/senha literais
**fictícios**, como demonstração do critério do professor. Não usa a conta pessoal.
Para executá-la de verdade, o responsável pelo Oracle precisa provisionar esse
usuário com as tabelas e permissões. Não ative o perfil para o banco atual.
Os testes verificam a seleção dos perfis; o teste Oracle utiliza o perfil padrão.
Não é possível garantir a pontuação desse critério sem a validação do professor.

## Organização

`domain`: entidade, participantes, enums, regras e portas de integração/persistência.
`application`: criação, consulta, correção, exclusão e validações dos casos de uso.
`infrastructure`: Gemini, Jackson, JDBC, conexões e DAO transacional.
`web`: controllers, contratos HTTP, CORS opcional e erros padronizados.
Nenhuma nova dependência Java foi necessária para esta finalização.
