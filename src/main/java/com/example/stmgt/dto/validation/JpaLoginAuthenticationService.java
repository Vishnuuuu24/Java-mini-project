package com.example.stmgt.dto.validation;

import com.example.stmgt.domain.entity.CustomUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JpaLoginAuthenticationService implements LoginAuthenticationService {

    private final DjangoPasswordVerifier passwordVerifier;

    public JpaLoginAuthenticationService(DjangoPasswordVerifier passwordVerifier) {
        this.passwordVerifier = passwordVerifier;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean authenticate(String username, String rawPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        String candidatePassword = rawPassword == null ? "" : rawPassword;

        List<CustomUser> users = entityManager.createQuery(
                "select u from CustomUser u where u.username = :username and u.isActive = true",
                CustomUser.class
            )
            .setParameter("username", normalizedUsername)
            .setMaxResults(1)
            .getResultList();

        if (users.isEmpty()) {
            return false;
        }

        String storedPassword = users.get(0).getPassword();
        return passwordVerifier.matches(candidatePassword, storedPassword);
    }
}
