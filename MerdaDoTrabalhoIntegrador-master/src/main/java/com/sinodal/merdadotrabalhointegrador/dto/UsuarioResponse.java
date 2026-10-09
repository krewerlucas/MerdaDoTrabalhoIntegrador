package com.sinodal.merdadotrabalhointegrador.dto;

import com.sinodal.merdadotrabalhointegrador.model.Role;
import com.sinodal.merdadotrabalhointegrador.model.Usuario;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String login,
        String tipo,
        String perfil,
        boolean podeGerenciarUsuarios,
        List<String> roles) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getLogin(),
                usuario.getTipo(),
                // POLIMORFISMO: o método executado depende da classe real (Administrador ou Cliente)
                usuario.descreverPerfil(),
                usuario.podeGerenciarUsuarios(),
                usuario.getRoles().stream().map(Role::getNome).sorted().toList());
    }
}
