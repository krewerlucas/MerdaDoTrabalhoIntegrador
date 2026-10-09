package com.sinodal.merdadotrabalhointegrador.dto;

import com.sinodal.merdadotrabalhointegrador.model.Role;

public record RoleResponse(Long id, String nome, String descricao) {

    public static RoleResponse de(Role role) {
        return new RoleResponse(role.getId(), role.getNome(), role.getDescricao());
    }
}
