package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomUserRepository extends JpaRepository<CustomUser, Long> {

    Optional<CustomUser> findByUsername(String username);

    List<CustomUser> findByRole(UserRole role);

    long countByRole(UserRole role);

    boolean existsByIdAndRole(Long id, UserRole role);
}
