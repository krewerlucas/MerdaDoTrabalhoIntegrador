package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.UsuarioRequest;
import com.sinodal.merdadotrabalhointegrador.dto.UsuarioResponse;

import java.util.List;

// INTERFACE: o controller depende deste contrato, não da implementação
public interface UsuarioService {

    List<UsuarioResponse> listar();

    UsuarioResponse buscarPorId(Long id);

    UsuarioResponse criar(UsuarioRequest request);

    UsuarioResponse atualizar(Long id, UsuarioRequest request);

    void remover(Long id);

    UsuarioResponse atribuirRole(Long id, String nomeRole);
}
