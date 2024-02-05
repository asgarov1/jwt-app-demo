package com.asgarov.jwtdemoapp.repository;

import com.asgarov.jwtdemoapp.domain.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    AppRole findByName(String name);

}
