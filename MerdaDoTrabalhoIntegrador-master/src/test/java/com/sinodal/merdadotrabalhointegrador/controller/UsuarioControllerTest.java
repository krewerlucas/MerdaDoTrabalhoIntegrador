package com.sinodal.merdadotrabalhointegrador.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarRetornaUsuariosComPerfilPolimorfico() throws Exception {
        criar("lista", "CLIENTE");

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].login", hasItems("admin", "lista")))
                .andExpect(jsonPath("$[*].perfil", hasItems(
                        "Administrador Admin Principal - acesso total ao sistema",
                        "Cliente Lista - acesso padrão")))
                .andExpect(jsonPath("$[*].senha").isEmpty());
    }

    @Test
    void buscarPorIdRetorna200() throws Exception {
        long id = criar("busca", "CLIENTE");

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.login").value("busca"));
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/usuarios/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"));
    }

    @Test
    void criarClienteRetorna201SemSenha() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("maria", "CLIENTE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipo").value("CLIENTE"))
                .andExpect(jsonPath("$.perfil").value("Cliente Maria - acesso padrão"))
                .andExpect(jsonPath("$.podeGerenciarUsuarios").value(false))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void criarAdministradorRecebeRoleAdmin() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("chefe", "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ADMIN"))
                .andExpect(jsonPath("$.podeGerenciarUsuarios").value(true))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")));
    }

    @Test
    void loginDuplicadoRetorna409() throws Exception {
        criar("joao", "CLIENTE");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("joao", "CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Login já cadastrado: joao"));
    }

    @Test
    void requisicaoInvalidaRetorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "invalido", "login": "x", "senha": "1", "tipo": "OUTRO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void jsonSemLoginRetorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Sem Login", "email": "semlogin@escola.com", "senha": "senha123", "tipo": "CLIENTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("login")));
    }

    @Test
    void jsonComRolesExistentesAssociaAsRoles() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Com Roles", "email": "comroles@escola.com", "login": "comroles",
                                 "senha": "senha123", "tipo": "CLIENTE", "roles": ["ROLE_ADMIN"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_ADMIN")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void jsonComRoleInexistenteRetorna409() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Role Ruim", "email": "roleruim@escola.com", "login": "roleruim",
                                 "senha": "senha123", "tipo": "CLIENTE", "roles": ["ROLE_FANTASMA"]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Role inexistente: ROLE_FANTASMA"));
    }

    @Test
    void atualizarRetorna200() throws Exception {
        long id = criar("pedro", "CLIENTE");

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Pedro Silva", "email": "pedro.silva@escola.com", "login": "pedro",
                                 "senha": "novaSenha1", "tipo": "CLIENTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pedro Silva"))
                .andExpect(jsonPath("$.email").value("pedro.silva@escola.com"))
                .andExpect(jsonPath("$.perfil").value("Cliente Pedro Silva - acesso padrão"));
    }

    @Test
    void atualizarTrocandoTipoRetorna409() throws Exception {
        long id = criar("ana", "CLIENTE");

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana", "ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Não é permitido alterar o tipo do usuario"));
    }

    @Test
    void removerRetorna204EDepois404() throws Exception {
        long id = criar("temporario", "CLIENTE");

        mockMvc.perform(delete("/api/usuarios/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void atribuirRoleRetorna200() throws Exception {
        long id = criar("promovido", "CLIENTE");

        mockMvc.perform(post("/api/usuarios/{id}/roles/{nomeRole}", id, "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")));

        mockMvc.perform(post("/api/usuarios/{id}/roles/{nomeRole}", id, "ROLE_ADMIN"))
                .andExpect(status().isConflict());
    }

    @Test
    void atribuirRoleInexistenteRetorna404() throws Exception {
        long id = criar("semrole", "CLIENTE");

        mockMvc.perform(post("/api/usuarios/{id}/roles/{nomeRole}", id, "ROLE_INEXISTENTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Role não encontrada: ROLE_INEXISTENTE"));
    }

    private long criar(String login, String tipo) throws Exception {
        String corpo = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login, tipo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private static String json(String login, String tipo) {
        String nome = Character.toUpperCase(login.charAt(0)) + login.substring(1);
        return """
                {"nome": "%s", "email": "%s@escola.com", "login": "%s", "senha": "senha123", "tipo": "%s"}
                """.formatted(nome, login, login, tipo);
    }
}
