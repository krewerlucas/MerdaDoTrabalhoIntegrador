package com.sinodal.merdadotrabalhointegrador.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

// HERANÇA: Usuario é uma Pessoa; Administrador e Cliente são Usuarios (tabela única com coluna "tipo")
@Slf4j
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
@Getter
@Setter
@NoArgsConstructor
public abstract class Usuario extends Pessoa {

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean ativo = true;

    // AGREGAÇÃO: Roles existem por conta própria e são compartilhadas entre usuários
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    protected Usuario(String nome, String email, String login, String senha) {
        super(nome, email);
        this.login = login;
        this.senha = senha;
    }

    public void adicionarRole(Role role) {
        if (role == null) {
            log.warn("Tentativa de adicionar role nula ao usuario {}", login);
            return;
        }
        if (roles.add(role)) {
            log.debug("Role {} adicionada ao usuario {}", role.getNome(), login);
        }
    }

    public void removerRole(Role role) {
        if (roles.remove(role)) {
            log.debug("Role {} removida do usuario {}", role.getNome(), login);
        }
    }

    public boolean possuiRole(String nomeRole) {
        return roles.stream().anyMatch(r -> r.getNome().equalsIgnoreCase(nomeRole));
    }

    // POLIMORFISMO (sobrecarga): mesmo nome, parâmetro diferente
    public boolean possuiRole(Role role) {
        return role != null && possuiRole(role.getNome());
    }

    public boolean podeGerenciarUsuarios() {
        return false;
    }

    public abstract String getTipo();
}
