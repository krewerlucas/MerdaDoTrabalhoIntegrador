package com.sinodal.merdadotrabalhointegrador.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos");
    }
}
