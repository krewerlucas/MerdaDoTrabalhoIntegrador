package com.sinodal.merdadotrabalhointegrador.exception;

// Erro do ARQUIVO inteiro (inexistente, sem permissão, falha de I/O). Erro de linha não usa esta classe.
public class ArquivoUsuariosException extends RuntimeException {

    public ArquivoUsuariosException(String mensagem) {
        super(mensagem);
    }

    public ArquivoUsuariosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
