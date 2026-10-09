package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.UsuarioRequest;
import com.sinodal.merdadotrabalhointegrador.exception.ArquivoUsuariosException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Só transforma texto em objetos. Não grava, não conhece HTTP, não conhece repository.
@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioArquivoReader {

    private static final String CABECALHO = "tipo;login;nome;email;senha;roles";

    private final Validator validator;

    /** Resultado de UMA linha: ou tem request, ou tem erro. */
    public record LinhaLida(int numero, UsuarioRequest request, String erro) {
        public boolean valida() {
            return erro == null;
        }
    }

    /**
     * Lê o arquivo inteiro. Erro de linha vira LinhaLida com erro (a leitura continua).
     * Arquivo inexistente, sem permissão ou falha de I/O lança ArquivoUsuariosException.
     */
    public List<LinhaLida> ler(Path arquivo) {
        List<LinhaLida> linhas = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String texto;
            int numero = 0;
            while ((texto = leitor.readLine()) != null) {
                numero++;
                if (texto.isBlank() || texto.stripLeading().startsWith("#")) {
                    continue;
                }
                if (numero == 1 && texto.strip().equalsIgnoreCase(CABECALHO)) {
                    continue;
                }
                linhas.add(lerLinha(numero, texto));
            }
        } catch (NoSuchFileException e) {
            log.warn("arquivo de usuários não encontrado: {}", arquivo);
            throw new ArquivoUsuariosException("Arquivo não encontrado: " + arquivo, e);
        } catch (AccessDeniedException e) {
            log.warn("sem permissão para ler o arquivo de usuários: {}", arquivo);
            throw new ArquivoUsuariosException("Sem permissão de leitura: " + arquivo, e);
        } catch (IOException e) {
            log.error("falha de I/O lendo o arquivo de usuários {}", arquivo, e);
            throw new ArquivoUsuariosException("Falha ao ler o arquivo: " + arquivo, e);
        }
        return linhas;
    }

    private LinhaLida lerLinha(int numero, String texto) {
        try {
            // split com -1 mantém campos vazios no fim (ex.: roles em branco)
            String[] campos = texto.split(";", -1);
            if (campos.length != 6) {
                throw new IllegalArgumentException(
                        "esperados 6 campos (" + CABECALHO + ") mas vieram " + campos.length);
            }
            String tipo = campos[0].strip().toUpperCase();
            String login = campos[1].strip();
            log.debug("lendo linha {}: login={}", numero, login);

            if (!tipo.equals("ADMIN") && !tipo.equals("CLIENTE")) {
                throw new IllegalArgumentException("tipo desconhecido: '" + campos[0].strip() + "' (use ADMIN ou CLIENTE)");
            }
            List<String> roles = Arrays.stream(campos[5].split(","))
                    .map(String::strip)
                    .filter(r -> !r.isEmpty())
                    .toList();

            UsuarioRequest request = new UsuarioRequest(
                    campos[2].strip(), campos[3].strip(), login, campos[4], tipo, roles);

            // mesmas regras do JSON (e-mail válido, tamanhos, obrigatórios)
            var violacoes = validator.validate(request);
            if (!violacoes.isEmpty()) {
                throw new IllegalArgumentException(violacoes.stream()
                        .map((ConstraintViolation<UsuarioRequest> v) -> v.getPropertyPath() + ": " + v.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; ")));
            }
            return new LinhaLida(numero, request, null);
        } catch (IllegalArgumentException e) {
            log.warn("linha {} inválida: {}", numero, e.getMessage());
            return new LinhaLida(numero, null, e.getMessage());
        }
    }
}
