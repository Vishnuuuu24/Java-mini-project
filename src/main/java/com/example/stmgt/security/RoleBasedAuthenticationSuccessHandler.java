package com.example.stmgt.security;

import com.example.stmgt.controller.SessionAuthHelper;
import com.example.stmgt.domain.enums.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (principal instanceof StmgtUserPrincipal userPrincipal) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionAuthHelper.SESSION_USER_ID, userPrincipal.getUserId());
        }

        String targetUrl = resolveTargetUrl(authentication == null ? null : authentication.getAuthorities());
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        response.sendRedirect(contextPath + targetUrl);
    }

    private String resolveTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        if (hasAuthority(authorities, UserRole.ADMIN)) {
            return "/users/admin/dashboard/";
        }
        if (hasAuthority(authorities, UserRole.FACULTY)) {
            return "/users/faculty/dashboard/";
        }
        return "/users/student/dashboard/";
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, UserRole role) {
        if (authorities == null || role == null) {
            return false;
        }

        String required = "ROLE_" + role.name();
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(required::equals);
    }
}
