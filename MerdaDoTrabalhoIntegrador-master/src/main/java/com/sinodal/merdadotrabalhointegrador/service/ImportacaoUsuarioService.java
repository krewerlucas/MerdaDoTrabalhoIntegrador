package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.FalhaImportacao;
import com.sinodal.merdadotrabalhointegrador.dto.ResumoImportacao;
import com.sinodal.merdadotrabalhointegrador.exception.RecursoNaoEncontradoException;
import com.sinodal.merdadotrabalhointegrador.exception.RegraNegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// NÃO é @Transactional de propósito: cada linha grava na sua própria transação (UsuarioService.criar),
// então uma linha ruim não desfaz as boas.
@Slf4j
@Service
public class ImportacaoUsuarioService {

    private final UsuarioArquivoReader leitor;
    private final UsuarioService usuarioService;
    private final Path arquivo;

    public ImportacaoUsuarioService(UsuarioArquivoReader leitor,
                                    UsuarioService usuarioService,
                                    @Value("${app.usuarios.arquivo}") String caminho) {
        this.leitor = leitor;
        this.usuarioService = usuarioService;
        this.arquivo = Path.of(caminho);
    }

    public ResumoImportacao importar() {
        log.debug("importando usuários de {}", arquivo.toAbsolutePath());
        List<UsuarioArquivoReader.LinhaLida> linhas = leitor.ler(arquivo);

        int gravadas = 0;
        List<FalhaImportacao> falhas = new ArrayList<>();

        for (UsuarioArquivoReader.LinhaLida linha : linhas) {
            if (!linha.valida()) {
                falhas.add(new FalhaImportacao(linha.numero(), linha.erro()));
                continue;
            }
            try {
                // MESMO método do POST JSON
                usuarioService.criar(linha.request());
                gravadas++;
            } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
                log.warn("linha {} ignorada: {}", linha.numero(), e.getMessage());
                falhas.add(new FalhaImportacao(linha.numero(), e.getMessage()));
            } catch (RuntimeException e) {
                log.error("falha inesperada na linha {}", linha.numero(), e);
                falhas.add(new FalhaImportacao(linha.numero(), "erro inesperado: " + e.getMessage()));
            }
        }

        log.info("importação concluída: {} lidas, {} gravadas, {} falhas", linhas.size(), gravadas, falhas.size());
        return new ResumoImportacao(linhas.size(), gravadas, falhas);
    }
}
