package com.asgarov.jwtdemoapp.services;

import com.asgarov.jwtdemoapp.config.PasswordEncoderConfig;
import com.asgarov.jwtdemoapp.domain.entity.AppRole;
import com.asgarov.jwtdemoapp.domain.entity.AppUser;
import com.asgarov.jwtdemoapp.domain.entity.Status;
import com.asgarov.jwtdemoapp.repository.AppRoleRepository;
import com.asgarov.jwtdemoapp.repository.AppUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class BootstrapService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void bootstrap() {
        if (appRoleRepository.findAll().isEmpty()) {
            this.appRoleRepository.save(new AppRole("USER"));
        }

        String email = "max@example.com";
        if (appUserRepository.findByEmail(email).isEmpty()) {
            AppRole userRole = appRoleRepository.findByName("USER");
            AppUser user = new AppUser(
                    email,
                    passwordEncoder.encode("password"),
                    "Max",
                    "Musterman",
                    Status.ACTIVE,
                    Set.of(userRole)
            );
            this.appUserRepository.save(user);
        }
    }
}
