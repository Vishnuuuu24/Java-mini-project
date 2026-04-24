package com.example.stmgt.dto;

import com.example.stmgt.domain.enums.AcademicLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Service-layer validation contract:
 * - userId must refer to an existing user with role STUDENT.
 * - userId must not already be linked to another student profile.
 * - registerNo must be unique.
 * - Every courseId must exist.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentCreateRequestDto {

    @NotNull(message = "Student user id is required")
    private Long userId;

    @NotBlank(message = "Register number is required")
    @Size(max = 12, message = "Register number must be at most 12 characters")
    private String registerNo;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @NotNull(message = "Attendance is required")
    private Double attendance;

    @NotNull(message = "Level is required")
    private AcademicLevel level;

    private Set<Long> courseIds = new HashSet<>();
}
