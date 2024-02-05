package com.asgarov.jwtdemoapp.repository;

import com.asgarov.jwtdemoapp.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}

