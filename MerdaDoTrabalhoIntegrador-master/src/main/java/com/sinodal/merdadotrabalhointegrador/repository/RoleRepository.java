package com.sinodal.merdadotrabalhointegrador.repository;

import com.sinodal.merdadotrabalhointegrador.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNome(String nome);
}
