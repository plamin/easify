package com.totvs.easifyai.web;

import com.totvs.easifyai.domain.AnaliseIndisponivelException;
import com.totvs.easifyai.domain.TranscricaoInvalidaException;
import com.totvs.easifyai.application.AnaliseNaoEncontradaException;
import com.totvs.easifyai.application.DadosInvalidosException;
import com.totvs.easifyai.infrastructure.persistence.PersistenciaException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento centralizado de exceções da API.
 *
 * <p>Evita {@code try/catch} espalhado pelos controllers e garante que toda
 * falha vire uma resposta HTTP consistente ({@link ErroResposta}) com o status
 * adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TranscricaoInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarTranscricaoInvalida(TranscricaoInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de("transcricao_invalida", e.getMessage()));
    }

    @ExceptionHandler(AnaliseIndisponivelException.class)
    public ResponseEntity<ErroResposta> tratarAnaliseIndisponivel(AnaliseIndisponivelException e) {
        log.warn("Falha na integração de análise: {}", e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErroResposta.de("analise_indisponivel",
                        "Não foi possível concluir a análise no momento. Tente novamente."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception e) {
        log.error("Falha interna de tipo {}", e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResposta.de("erro_interno", "Erro interno inesperado."));
    }
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(DadosInvalidosException e) {
        return ResponseEntity.badRequest().body(ErroResposta.de("dados_invalidos", e.getMessage()));
    }

    @ExceptionHandler(AnaliseNaoEncontradaException.class)
    public ResponseEntity<ErroResposta> naoEncontrada(AnaliseNaoEncontradaException e) {
        return ResponseEntity.status(404).body(ErroResposta.de("analise_nao_encontrada", e.getMessage()));
    }

    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroResposta> persistencia(PersistenciaException e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && sql.getSQLState() != null
                    && sql.getSQLState().startsWith("23")) {
                return ResponseEntity.status(409).body(ErroResposta.de("conflito_dados",
                        "A operação conflita com os dados existentes."));
            }
        }
        return tratarErroInesperado(e);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensagem = switch (status.value()) {
            case 400 -> "Requisição inválida. Verifique o JSON, os campos, os enums e o UUID informado.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 415 -> "Tipo de conteúdo não suportado. Envie application/json.";
            default -> "Não foi possível processar a requisição.";
        };
        return new ResponseEntity<>(ErroResposta.de("http_" + status.value(), mensagem), headers, status);
    }
}
