package com.sinodal.merdadotrabalhointegrador.controller;

import com.sinodal.merdadotrabalhointegrador.dto.ResumoImportacao;
import com.sinodal.merdadotrabalhointegrador.service.ImportacaoUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Não abre arquivo nem acessa repository: só chama o service.
@Slf4j
@RestController
@RequestMapping("/api/usuarios/importar")
@RequiredArgsConstructor
public class ImportacaoUsuarioController {

    private final ImportacaoUsuarioService importacaoService;

    @PostMapping
    public ResumoImportacao importar() {
        log.debug("POST /api/usuarios/importar");
        return importacaoService.importar();
    }
}
