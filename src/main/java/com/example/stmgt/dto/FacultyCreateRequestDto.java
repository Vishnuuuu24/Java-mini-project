package com.example.stmgt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Service-layer validation contract:
 * - userId must refer to an existing user with role FACULTY.
 * - userId must not already be linked to another faculty profile.
 * - facultyId must be unique.
 * - Email should be derived from the selected user, mirroring Django clean/save behavior.
 */
@Getter
@Setter
@NoArgsConstructor
public class FacultyCreateRequestDto {

    @NotNull(message = "Faculty user id is required")
    private Long userId;

    @NotNull(message = "Faculty id is required")
    @Min(value = 100, message = "Faculty id must be at least 100")
    @Max(value = 999, message = "Faculty id must be at most 999")
    private Integer facultyId;

    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @Size(max = 100, message = "Designation must be at most 100 characters")
    private String designation;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(max = 15, message = "Phone must be at most 15 characters")
    private String phone;

    private Set<Long> courseIds = new HashSet<>();
}
