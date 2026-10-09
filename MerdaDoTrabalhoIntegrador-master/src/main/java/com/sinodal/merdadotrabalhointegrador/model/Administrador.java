package com.sinodal.merdadotrabalhointegrador.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class Administrador extends Usuario {

    public Administrador(String nome, String email, String login, String senha) {
        super(nome, email, login, senha);
    }

    // POLIMORFISMO (sobrescrita)
    @Override
    public String descreverPerfil() {
        return "Administrador " + getNome() + " - acesso total ao sistema";
    }

    @Override
    public boolean podeGerenciarUsuarios() {
        return true;
    }

    @Override
    public String getTipo() {
        return "ADMIN";
    }
}
