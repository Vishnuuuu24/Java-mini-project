package com.example.stmgt.security;

import com.example.stmgt.controller.DjangoPasswordHasher;
import com.example.stmgt.dto.validation.DjangoPasswordVerifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DjangoPasswordEncoder implements PasswordEncoder {

    private final DjangoPasswordVerifier passwordVerifier;
    private final DjangoPasswordHasher passwordHasher;

    public DjangoPasswordEncoder(DjangoPasswordVerifier passwordVerifier, DjangoPasswordHasher passwordHasher) {
        this.passwordVerifier = passwordVerifier;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }

        return passwordHasher.hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            return false;
        }

        return passwordVerifier.matches(rawPassword.toString(), encodedPassword);
    }
}
