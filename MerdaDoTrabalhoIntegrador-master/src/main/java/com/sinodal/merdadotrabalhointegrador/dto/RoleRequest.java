package com.sinodal.merdadotrabalhointegrador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank @Pattern(regexp = "ROLE_[A-Z_]+", message = "deve seguir o formato ROLE_NOME") String nome,
        @Size(max = 200) String descricao) {
}
