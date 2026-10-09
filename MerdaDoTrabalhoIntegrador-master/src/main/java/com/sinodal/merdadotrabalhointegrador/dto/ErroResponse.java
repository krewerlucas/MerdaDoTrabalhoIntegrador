package com.sinodal.merdadotrabalhointegrador.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErroResponse(int status, String erro, String mensagem, LocalDateTime timestamp) {

    public static ErroResponse de(HttpStatus status, String mensagem) {
        return new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, LocalDateTime.now());
    }
}
