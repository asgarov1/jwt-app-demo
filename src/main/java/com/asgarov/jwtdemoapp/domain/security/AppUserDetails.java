package com.asgarov.jwtdemoapp.domain.security;

import com.asgarov.jwtdemoapp.domain.entity.AppRole;
import com.asgarov.jwtdemoapp.domain.entity.AppUser;
import com.asgarov.jwtdemoapp.domain.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<AppRole> roles = new ArrayList<>();
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles().stream()
                .map(AppRole::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public static AppUserDetails of(AppUser user) {
        return new AppUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getAppRoles(),
                true,
                true,
                true,
                Status.ACTIVE.equals(user.getStatus())
        );
    }

    public List<String> getRoleNames() {
        return getRoles().stream().map(AppRole::getName).collect(Collectors.toList());
    }
}
