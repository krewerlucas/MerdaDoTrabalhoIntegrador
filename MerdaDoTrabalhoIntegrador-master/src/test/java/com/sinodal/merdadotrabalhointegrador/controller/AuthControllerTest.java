package com.sinodal.merdadotrabalhointegrador.controller;

import com.sinodal.merdadotrabalhointegrador.repository.RegistroAcessoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistroAcessoRepository registroAcessoRepository;

    @Test
    void loginComSenhaCorretaRetorna200() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "admin", "senha": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Bem-vindo, Admin Principal!"))
                .andExpect(jsonPath("$.usuario.login").value("admin"))
                .andExpect(jsonPath("$.usuario.tipo").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.perfil").value("Administrador Admin Principal - acesso total ao sistema"))
                .andExpect(jsonPath("$.usuario.podeGerenciarUsuarios").value(true))
                .andExpect(jsonPath("$.usuario.roles", hasItems("ROLE_ADMIN", "ROLE_USER")))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void loginComSenhaErradaRetorna401EGravaRegistro() throws Exception {
        long antes = registroAcessoRepository.count();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "admin", "senha": "errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Unauthorized"))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));

        assertThat(registroAcessoRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    void loginInexistenteRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "ninguem", "senha": "qualquer"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "", "senha": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido"));
    }
}
