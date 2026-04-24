package com.example.stmgt.dto.validation;

/**
 * Authentication contract used by login DTO validation.
 */
public interface LoginAuthenticationService {

    boolean authenticate(String username, String rawPassword);
}
