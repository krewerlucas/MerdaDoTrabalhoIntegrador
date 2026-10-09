package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.LoginRequest;
import com.sinodal.merdadotrabalhointegrador.dto.LoginResponse;
import com.sinodal.merdadotrabalhointegrador.dto.UsuarioResponse;
import com.sinodal.merdadotrabalhointegrador.exception.CredenciaisInvalidasException;
import com.sinodal.merdadotrabalhointegrador.model.RegistroAcesso;
import com.sinodal.merdadotrabalhointegrador.model.Usuario;
import com.sinodal.merdadotrabalhointegrador.repository.RegistroAcessoRepository;
import com.sinodal.merdadotrabalhointegrador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RegistroAcessoRepository registroAcessoRepository;
    private final PasswordEncoder passwordEncoder;

    // noRollbackFor: o registro da tentativa falha precisa ficar gravado mesmo com a exceção
    @Transactional(noRollbackFor = CredenciaisInvalidasException.class)
    public LoginResponse login(LoginRequest request) {
        log.debug("Tentativa de login: {}", request.login());

        Usuario usuario = usuarioRepository.findByLogin(request.login()).orElseThrow(() -> {
            log.warn("Login inexistente: {}", request.login());
            return new CredenciaisInvalidasException();
        });

        boolean sucesso = usuario.isAtivo() && passwordEncoder.matches(request.senha(), usuario.getSenha());
        registroAcessoRepository.save(new RegistroAcesso(usuario, sucesso));

        if (!sucesso) {
            log.warn("Senha inválida ou usuario inativo: {}", request.login());
            throw new CredenciaisInvalidasException();
        }

        log.info("Login realizado com sucesso: {} ({})", usuario.getLogin(), usuario.getTipo());
        return new LoginResponse("Bem-vindo, " + usuario.getNome() + "!", UsuarioResponse.de(usuario));
    }
}
