package com.sinodal.merdadotrabalhointegrador.service;

import com.sinodal.merdadotrabalhointegrador.dto.RoleRequest;
import com.sinodal.merdadotrabalhointegrador.dto.RoleResponse;
import com.sinodal.merdadotrabalhointegrador.exception.RegraNegocioException;
import com.sinodal.merdadotrabalhointegrador.model.Role;
import com.sinodal.merdadotrabalhointegrador.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> listar() {
        return roleRepository.findAll().stream().map(RoleResponse::de).toList();
    }

    @Transactional
    public RoleResponse criar(RoleRequest request) {
        if (roleRepository.findByNome(request.nome()).isPresent()) {
            log.warn("Role já cadastrada: {}", request.nome());
            throw new RegraNegocioException("Role já cadastrada: " + request.nome());
        }
        Role salva = roleRepository.save(new Role(request.nome(), request.descricao()));
        log.info("Role criada: {}", salva.getNome());
        return RoleResponse.de(salva);
    }
}
