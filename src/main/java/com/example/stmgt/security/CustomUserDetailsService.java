package com.example.stmgt.security;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.repository.CustomUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomUserRepository customUserRepository;

    public CustomUserDetailsService(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username == null ? "" : username.trim();

        CustomUser user = customUserRepository.findByUsername(normalized)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalized));

        return StmgtUserPrincipal.fromUser(user);
    }
}
