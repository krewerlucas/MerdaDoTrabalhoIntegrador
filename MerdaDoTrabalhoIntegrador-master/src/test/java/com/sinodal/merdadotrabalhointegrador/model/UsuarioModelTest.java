package com.sinodal.merdadotrabalhointegrador.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioModelTest {

    @Test
    void descreverPerfilDependeDaClasseReal() {
        List<Usuario> usuarios = List.of(
                new Administrador("Ana", "ana@escola.com", "ana", "hash"),
                new Cliente("Bruno", "bruno@escola.com", "bruno", "hash"));

        assertThat(usuarios).extracting(Usuario::descreverPerfil).containsExactly(
                "Administrador Ana - acesso total ao sistema",
                "Cliente Bruno - acesso padrão");
        assertThat(usuarios).extracting(Usuario::podeGerenciarUsuarios).containsExactly(true, false);
        assertThat(usuarios).extracting(Usuario::getTipo).containsExactly("ADMIN", "CLIENTE");
    }

    @Test
    void rolesSaoAgregadasECompartilhadas() {
        Role roleUser = new Role("ROLE_USER", "Acesso padrão");
        Usuario ana = new Administrador("Ana", "ana@escola.com", "ana", "hash");
        Usuario bruno = new Cliente("Bruno", "bruno@escola.com", "bruno", "hash");

        ana.adicionarRole(roleUser);
        bruno.adicionarRole(roleUser);
        ana.adicionarRole(roleUser);

        assertThat(ana.getRoles()).hasSize(1);
        assertThat(ana.possuiRole("role_user")).isTrue();
        assertThat(bruno.possuiRole(roleUser)).isTrue();

        ana.removerRole(roleUser);

        assertThat(ana.possuiRole("ROLE_USER")).isFalse();
        assertThat(bruno.possuiRole("ROLE_USER")).isTrue();
    }

    @Test
    void adicionarRoleNulaEhIgnorado() {
        Usuario cliente = new Cliente("Carla", "carla@escola.com", "carla", "hash");

        cliente.adicionarRole(null);

        assertThat(cliente.getRoles()).isEmpty();
        assertThat(cliente.possuiRole((Role) null)).isFalse();
    }
}
