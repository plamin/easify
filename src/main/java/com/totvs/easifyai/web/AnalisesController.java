package com.totvs.easifyai.web;

import com.totvs.easifyai.application.AnalisarTranscricaoUseCase;
import com.totvs.easifyai.application.AtualizarAnaliseCommand;
import com.totvs.easifyai.application.GerenciarAnalisesService;
import com.totvs.easifyai.domain.RegistroAcompanhamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analises")
public class AnalisesController {
    private final AnalisarTranscricaoUseCase analisar;
    private final GerenciarAnalisesService gerenciar;

    public AnalisesController(AnalisarTranscricaoUseCase analisar, GerenciarAnalisesService gerenciar) {
        this.analisar = analisar;
        this.gerenciar = gerenciar;
    }

    @PostMapping
    public ResponseEntity<AnaliseResponse> criar(@RequestBody AnalisarTranscricaoRequest request) {
        var analise = analisar.executar(request.getTranscricao());
        return ResponseEntity.created(URI.create("/analises/" + analise.getId()))
                .body(AnaliseResponse.de(analise));
    }

    @GetMapping
    public List<AnaliseResponse> listar() {
        return gerenciar.listar().stream().map(AnaliseResponse::de).toList();
    }

    @GetMapping("/{id}")
    public AnaliseResponse buscar(@PathVariable UUID id) {
        return AnaliseResponse.de(gerenciar.buscar(id));
    }

    @PutMapping("/{id}")
    public AnaliseResponse atualizar(@PathVariable UUID id, @RequestBody AtualizarAnaliseCommand request) {
        return AnaliseResponse.de(gerenciar.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        gerenciar.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transcricao")
    public TranscricaoResponse transcricao(@PathVariable UUID id) {
        return new TranscricaoResponse(id, gerenciar.buscarTranscricao(id));
    }

    @GetMapping("/{id}/acompanhamentos")
    public List<RegistroAcompanhamento> historico(@PathVariable UUID id) {
        return gerenciar.listarHistorico(id);
    }

    @PostMapping("/{id}/acompanhamentos")
    public ResponseEntity<Void> registrarStatus(@PathVariable UUID id,
                                               @RequestBody RegistrarAcompanhamentoRequest request) {
        gerenciar.registrarStatus(id, request.status(), request.observacao());
        return ResponseEntity.noContent().build();
    }
}
