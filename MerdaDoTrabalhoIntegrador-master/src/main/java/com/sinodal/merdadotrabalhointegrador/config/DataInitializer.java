package com.sinodal.merdadotrabalhointegrador.config;

import com.sinodal.merdadotrabalhointegrador.model.Administrador;
import com.sinodal.merdadotrabalhointegrador.model.Role;
import com.sinodal.merdadotrabalhointegrador.repository.RoleRepository;
import com.sinodal.merdadotrabalhointegrador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.sinodal.merdadotrabalhointegrador.service.UsuarioServiceImpl.ROLE_ADMIN;
import static com.sinodal.merdadotrabalhointegrador.service.UsuarioServiceImpl.ROLE_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = criarRoleSeNaoExistir(ROLE_ADMIN, "Administra usuários e roles");
        Role user = criarRoleSeNaoExistir(ROLE_USER, "Acesso padrão ao sistema");

        if (usuarioRepository.existsByLogin("admin")) {
            log.debug("Usuario admin já existe, nada a fazer");
            return;
        }
        Administrador administrador = new Administrador(
                "Admin Principal", "admin@escola.com", "admin", passwordEncoder.encode("admin123"));
        administrador.adicionarRole(admin);
        administrador.adicionarRole(user);
        usuarioRepository.save(administrador);
        log.info("Usuario padrão criado: login=admin senha=admin123");
    }

    private Role criarRoleSeNaoExistir(String nome, String descricao) {
        return roleRepository.findByNome(nome).orElseGet(() -> {
            log.info("Criando role {}", nome);
            return roleRepository.save(new Role(nome, descricao));
        });
    }
}
