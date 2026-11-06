package com.totvs.easifyai.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** Evento do histórico, com data sem fuso conforme o TIMESTAMP do modelo. */
public record RegistroAcompanhamento(StatusAcompanhamento status, String observacao,
                                    LocalDateTime dataMudanca) {
    public RegistroAcompanhamento {
        Objects.requireNonNull(status);
        Objects.requireNonNull(dataMudanca);
    }
}