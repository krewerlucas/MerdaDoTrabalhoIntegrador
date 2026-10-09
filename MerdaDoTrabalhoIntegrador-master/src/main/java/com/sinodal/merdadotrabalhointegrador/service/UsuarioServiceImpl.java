package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.UsuarioRequest;
import com.sinodal.merdadotrabalhointegrador.dto.UsuarioResponse;
import com.sinodal.merdadotrabalhointegrador.exception.RecursoNaoEncontradoException;
import com.sinodal.merdadotrabalhointegrador.exception.RegraNegocioException;
import com.sinodal.merdadotrabalhointegrador.model.Administrador;
import com.sinodal.merdadotrabalhointegrador.model.Cliente;
import com.sinodal.merdadotrabalhointegrador.model.Role;
import com.sinodal.merdadotrabalhointegrador.model.Usuario;
import com.sinodal.merdadotrabalhointegrador.repository.RoleRepository;
import com.sinodal.merdadotrabalhointegrador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        // POLIMORFISMO: a lista é de Usuario, mas cada item é Administrador ou Cliente
        List<Usuario> usuarios = usuarioRepository.findAll();
        log.debug("Listando {} usuarios", usuarios.size());
        return usuarios.stream().map(UsuarioResponse::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(buscarEntidade(id));
    }

    @Override
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        log.debug("Criando usuario login={} tipo={}", request.login(), request.tipo());
        if (usuarioRepository.existsByLogin(request.login())) {
            log.warn("Login já cadastrado: {}", request.login());
            throw new RegraNegocioException("Login já cadastrado: " + request.login());
        }

        Usuario usuario = novoUsuario(request);
        associarRoles(usuario, request);

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            log.info("Usuario criado: id={} login={} tipo={}", salvo.getId(), salvo.getLogin(), salvo.getTipo());
            return UsuarioResponse.de(salvo);
        } catch (DataIntegrityViolationException e) {
            log.error("Falha de integridade ao salvar usuario {}", request.login(), e);
            throw new RegraNegocioException("Não foi possível salvar o usuario: dados duplicados ou inválidos", e);
        }
    }

    @Override
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);
        if (!usuario.getTipo().equals(request.tipo())) {
            log.warn("Tentativa de trocar tipo do usuario {} de {} para {}", id, usuario.getTipo(), request.tipo());
            throw new RegraNegocioException("Não é permitido alterar o tipo do usuario");
        }
        if (!usuario.getLogin().equals(request.login()) && usuarioRepository.existsByLogin(request.login())) {
            log.warn("Login já cadastrado: {}", request.login());
            throw new RegraNegocioException("Login já cadastrado: " + request.login());
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setLogin(request.login());
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            log.info("Usuario atualizado: id={} login={}", salvo.getId(), salvo.getLogin());
            return UsuarioResponse.de(salvo);
        } catch (DataIntegrityViolationException e) {
            log.error("Falha de integridade ao atualizar usuario {}", id, e);
            throw new RegraNegocioException("Não foi possível atualizar o usuario", e);
        }
    }

    @Override
    @Transactional
    public void remover(Long id) {
        Usuario usuario = buscarEntidade(id);
        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
            log.info("Usuario removido: id={} login={}", id, usuario.getLogin());
        } catch (DataIntegrityViolationException e) {
            log.warn("Usuario {} possui registros vinculados e não pode ser removido", id);
            throw new RegraNegocioException("Usuario possui registros de acesso e não pode ser removido", e);
        }
    }

    @Override
    @Transactional
    public UsuarioResponse atribuirRole(Long id, String nomeRole) {
        Usuario usuario = buscarEntidade(id);
        Role role = buscarRole(nomeRole);
        if (usuario.possuiRole(role)) {
            log.warn("Usuario {} já possui a role {}", usuario.getLogin(), nomeRole);
            throw new RegraNegocioException("Usuario já possui a role " + nomeRole);
        }
        usuario.adicionarRole(role);
        log.info("Role {} atribuída ao usuario {}", nomeRole, usuario.getLogin());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    // Sem roles no pedido: padrão (ROLE_USER, e ROLE_ADMIN para quem pode gerenciar usuários).
    // Com roles: cada nome precisa existir no banco; role inexistente é erro de negócio, não NullPointerException.
    private void associarRoles(Usuario usuario, UsuarioRequest request) {
        if (request.roles() == null || request.roles().isEmpty()) {
            usuario.adicionarRole(buscarRole(ROLE_USER));
            if (usuario.podeGerenciarUsuarios()) {
                usuario.adicionarRole(buscarRole(ROLE_ADMIN));
            }
            return;
        }
        for (String nome : request.roles()) {
            Role role = roleRepository.findByNome(nome).orElseThrow(() -> {
                log.warn("Role inexistente no cadastro de {}: {}", request.login(), nome);
                return new RegraNegocioException("Role inexistente: " + nome);
            });
            usuario.adicionarRole(role);
        }
    }

    private Usuario novoUsuario(UsuarioRequest request) {
        String hash = passwordEncoder.encode(request.senha());
        return switch (request.tipo()) {
            case "ADMIN" -> new Administrador(request.nome(), request.email(), request.login(), hash);
            case "CLIENTE" -> new Cliente(request.nome(), request.email(), request.login(), hash);
            default -> throw new RegraNegocioException("Tipo de usuario inválido: " + request.tipo());
        };
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> {
            log.warn("Usuario não encontrado: id={}", id);
            return new RecursoNaoEncontradoException("Usuario não encontrado: id=" + id);
        });
    }

    private Role buscarRole(String nome) {
        return roleRepository.findByNome(nome).orElseThrow(() -> {
            log.warn("Role não encontrada: {}", nome);
            return new RecursoNaoEncontradoException("Role não encontrada: " + nome);
        });
    }
}
