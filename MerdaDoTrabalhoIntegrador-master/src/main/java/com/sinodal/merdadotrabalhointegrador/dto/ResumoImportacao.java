package com.sinodal.merdadotrabalhointegrador.dto;

import java.util.List;

// linhasLidas = linhas com dados (vazias e comentários não contam)
public record ResumoImportacao(int linhasLidas, int gravadas, List<FalhaImportacao> falhas) {
}
