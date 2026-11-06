package com.totvs.easifyai.web;

import com.totvs.easifyai.domain.StatusAcompanhamento;

public record RegistrarAcompanhamentoRequest(StatusAcompanhamento status, String observacao) { }
