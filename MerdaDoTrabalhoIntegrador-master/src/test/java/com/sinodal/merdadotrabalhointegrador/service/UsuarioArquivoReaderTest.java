package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.exception.ArquivoUsuariosException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Teste unitário do leitor: sem Spring, sem banco.
class UsuarioArquivoReaderTest {

    @TempDir
    Path pasta;

    private final UsuarioArquivoReader leitor =
            new UsuarioArquivoReader(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void ignoraVaziasEComentariosEMantemONumeroFisicoDaLinha() throws IOException {
        Path arquivo = pasta.resolve("usuarios.txt");
        Files.writeString(arquivo, """
                # comentário

                CLIENTE;maria;Maria Silva;maria@escola.com;senha123;ROLE_USER
                ADMIN;joao;Joao Souza;joao@escola.com;senha123;ROLE_ADMIN,ROLE_USER
                """);

        List<UsuarioArquivoReader.LinhaLida> linhas = leitor.ler(arquivo);

        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(0).numero()).isEqualTo(3);
        assertThat(linhas.get(1).numero()).isEqualTo(4);
        assertThat(linhas.get(1).request().roles()).containsExactly("ROLE_ADMIN", "ROLE_USER");
        assertThat(linhas.get(1).request().tipo()).isEqualTo("ADMIN");
    }

    @Test
    void linhaRuimViraErroDaLinhaEALeituraContinua() throws IOException {
        Path arquivo = pasta.resolve("usuarios.txt");
        Files.writeString(arquivo, """
                CLIENTE;ana;Ana;ana@escola.com
                OUTRO;beto;Beto;beto@escola.com;senha123;ROLE_USER
                CLIENTE;caio;Caio;email-invalido;senha123;ROLE_USER
                CLIENTE;dani;Dani;dani@escola.com;senha123;ROLE_USER
                """);

        List<UsuarioArquivoReader.LinhaLida> linhas = leitor.ler(arquivo);

        assertThat(linhas).hasSize(4);
        assertThat(linhas.get(0).erro()).contains("esperados 6 campos");
        assertThat(linhas.get(1).erro()).contains("tipo desconhecido");
        assertThat(linhas.get(2).erro()).contains("email");
        assertThat(linhas.get(3).valida()).isTrue();
    }

    @Test
    void arquivoInexistenteLancaErroDoArquivoInteiro() {
        Path inexistente = pasta.resolve("nao-existe.txt");

        assertThatThrownBy(() -> leitor.ler(inexistente))
                .isInstanceOf(ArquivoUsuariosException.class)
                .hasMessageContaining("Arquivo não encontrado");
    }
}
