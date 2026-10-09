package com.sinodal.merdadotrabalhointegrador.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("CLIENTE")
@NoArgsConstructor
public class Cliente extends Usuario {

    public Cliente(String nome, String email, String login, String senha) {
        super(nome, email, login, senha);
    }

    // POLIMORFISMO (sobrescrita)
    @Override
    public String descreverPerfil() {
        return "Cliente " + getNome() + " - acesso padrão";
    }

    @Override
    public String getTipo() {
        return "CLIENTE";
    }
}
