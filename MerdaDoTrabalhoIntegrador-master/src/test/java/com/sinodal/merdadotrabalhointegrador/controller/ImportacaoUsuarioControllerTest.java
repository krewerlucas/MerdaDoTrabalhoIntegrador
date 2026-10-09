package com.sinodal.merdadotrabalhointegrador.controller;

import com.sinodal.merdadotrabalhointegrador.model.Usuario;
import com.sinodal.merdadotrabalhointegrador.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImportacaoUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // caminho vem do application.properties de teste (o mesmo mecanismo da aplicação real)
    @Value("${app.usuarios.arquivo}")
    private String caminho;

    @AfterEach
    void apagarArquivo() throws IOException {
        Files.deleteIfExists(Path.of(caminho));
    }

    @Test
    void importaUmaLinhaBoaEDuasRuinsComNumeroDaLinha() throws Exception {
        escrever("""
                CLIENTE;imp_maria;Maria Silva;imp_maria@escola.com;senha123;ROLE_USER
                OUTRO;imp_joao;Joao Souza;imp_joao@escola.com;senha123;ROLE_USER
                CLIENTE;imp_maria;Maria Repetida;imp_maria2@escola.com;senha123;ROLE_USER
                """);

        mockMvc.perform(post("/api/usuarios/importar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linhasLidas").value(3))
                .andExpect(jsonPath("$.gravadas").value(1))
                .andExpect(jsonPath("$.falhas.length()").value(2))
                .andExpect(jsonPath("$.falhas[0].linha").value(2))
                .andExpect(jsonPath("$.falhas[0].motivo").value(containsString("tipo desconhecido")))
                .andExpect(jsonPath("$.falhas[1].linha").value(3))
                .andExpect(jsonPath("$.falhas[1].motivo").value("Login já cadastrado: imp_maria"));

        // mesma tabela e mesma estrutura do JSON; senha com hash BCrypt
        Usuario gravado = usuarioRepository.findByLogin("imp_maria").orElseThrow();
        assertThat(gravado.getTipo()).isEqualTo("CLIENTE");
        assertThat(gravado.possuiRole("ROLE_USER")).isTrue();
        assertThat(gravado.getSenha()).startsWith("$2").isNotEqualTo("senha123");
        assertThat(usuarioRepository.findByLogin("imp_joao")).isEmpty();
    }

    @Test
    void ignoraVazioEComentarioETrataRoleInexistenteECampoAMenos() throws Exception {
        escrever("""
                # comentário

                ADMIN;imp_admin;Admin Imp;imp_admin@escola.com;senha123;ROLE_ADMIN,ROLE_USER
                CLIENTE;imp_semrole;Sem Role;imp_semrole@escola.com;senha123;ROLE_FANTASMA
                CLIENTE;imp_curta;Curta
                """);

        mockMvc.perform(post("/api/usuarios/importar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linhasLidas").value(3))
                .andExpect(jsonPath("$.gravadas").value(1))
                .andExpect(jsonPath("$.falhas[0].linha").value(4))
                .andExpect(jsonPath("$.falhas[0].motivo").value("Role inexistente: ROLE_FANTASMA"))
                .andExpect(jsonPath("$.falhas[1].linha").value(5))
                .andExpect(jsonPath("$.falhas[1].motivo").value(containsString("esperados 6 campos")));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(jsonPath("$[*].login", hasItem("imp_admin")));
    }

    @Test
    void arquivoInexistenteEErroDoArquivoInteiro() throws Exception {
        // o arquivo não foi criado
        mockMvc.perform(post("/api/usuarios/importar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("Arquivo não encontrado")));
    }

    @Test
    void mesmoLoginNoArquivoEDepoisNoJsonRetorna409() throws Exception {
        escrever("CLIENTE;imp_dup;Dup Um;imp_dup@escola.com;senha123;ROLE_USER\n");
        mockMvc.perform(post("/api/usuarios/importar"))
                .andExpect(jsonPath("$.gravadas").value(1));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Dup Dois", "email": "imp_dup2@escola.com", "login": "imp_dup",
                                 "senha": "senha123", "tipo": "CLIENTE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Login já cadastrado: imp_dup"));
    }

    private void escrever(String conteudo) throws IOException {
        Path arquivo = Path.of(caminho);
        Files.createDirectories(arquivo.toAbsolutePath().getParent());
        Files.writeString(arquivo, conteudo);
    }
}
