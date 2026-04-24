package com.example.stmgt.security;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class StmgtUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final UserRole role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    private StmgtUserPrincipal(
        Long userId,
        String username,
        String password,
        UserRole role,
        boolean active,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
        this.authorities = authorities;
    }

    public static StmgtUserPrincipal fromUser(CustomUser user) {
        Objects.requireNonNull(user, "user is required");

        return new StmgtUserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole(),
            user.isActive(),
            mapAuthorities(user.getRole())
        );
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    private static Collection<? extends GrantedAuthority> mapAuthorities(UserRole role) {
        if (role == null) {
            return List.of();
        }

        String authority = "ROLE_" + role.name();
        return List.of(new SimpleGrantedAuthority(authority));
    }
}
