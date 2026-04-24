package com.example.stmgt.service;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserLifecycleService {

    private final CustomUserRepository customUserRepository;

    public UserLifecycleService(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Transactional
    public CustomUser createUserAccount(
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        String encodedPassword
    ) {
        if (role == null) {
            throw new ValidationException("Role is required");
        }

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new ValidationException("Password is required");
        }

        String normalizedUsername = normalize(username);
        if (normalizedUsername.isBlank()) {
            throw new ValidationException("Username is required");
        }

        if (customUserRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new ValidationException("Username already exists");
        }

        CustomUser user = new CustomUser();
        user.setUsername(normalizedUsername);
        user.setEmail(normalize(email));
        user.setFirstName(normalize(firstName));
        user.setLastName(normalize(lastName));
        user.setRole(role);
        user.setPassword(encodedPassword);
        user.setStaff(role == UserRole.ADMIN);
        user.setActive(true);
        user.setSuperuser(role == UserRole.ADMIN);

        return customUserRepository.save(user);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
