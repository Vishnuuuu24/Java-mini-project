package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.service.exception.AuthorizationException;
import com.example.stmgt.service.exception.NotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SessionAuthHelper {

    public static final String SESSION_USER_ID = "currentUserId";

    private final CustomUserRepository customUserRepository;

    public SessionAuthHelper(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    public void signIn(HttpSession session, CustomUser user) {
        session.setAttribute(SESSION_USER_ID, user.getId());
    }

    public void signOut(HttpSession session) {
        session.invalidate();
    }

    public CustomUser requireAuthenticated(HttpSession session) {
        Long userId = resolveUserId(session);
        return customUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Authenticated user does not exist: " + userId));
    }

    public CustomUser requireRole(HttpSession session, UserRole... allowedRoles) {
        CustomUser user = requireAuthenticated(session);
        Set<UserRole> allowed = Arrays.stream(allowedRoles).collect(Collectors.toSet());

        if (!allowed.contains(user.getRole())) {
            throw new AuthorizationException("You are not authorized to access this page.");
        }

        return user;
    }

    private Long resolveUserId(HttpSession session) {
        Object raw = session.getAttribute(SESSION_USER_ID);
        if (raw == null) {
            throw new AuthorizationException("Authentication required.");
        }

        if (raw instanceof Long id) {
            return id;
        }
        if (raw instanceof Integer id) {
            return id.longValue();
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }

        throw new AuthorizationException("Invalid authentication state.");
    }
}
