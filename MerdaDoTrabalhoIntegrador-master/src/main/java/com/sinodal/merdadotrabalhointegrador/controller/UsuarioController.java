package com.sinodal.merdadotrabalhointegrador.controller;

import com.sinodal.merdadotrabalhointegrador.dto.UsuarioRequest;
import com.sinodal.merdadotrabalhointegrador.dto.UsuarioResponse;
import com.sinodal.merdadotrabalhointegrador.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar() {
        log.debug("GET /api/usuarios");
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        log.debug("GET /api/usuarios/{}", id);
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        log.debug("POST /api/usuarios login={}", request.login());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        log.debug("PUT /api/usuarios/{}", id);
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        log.debug("DELETE /api/usuarios/{}", id);
        usuarioService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{nomeRole}")
    public UsuarioResponse atribuirRole(@PathVariable Long id, @PathVariable String nomeRole) {
        log.debug("POST /api/usuarios/{}/roles/{}", id, nomeRole);
        return usuarioService.atribuirRole(id, nomeRole);
    }
}
