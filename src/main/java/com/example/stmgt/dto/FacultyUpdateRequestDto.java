package com.example.stmgt.dto;

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
 * Service-layer validation contract for admin faculty profile edit:
 * - user linkage is immutable during update and is not accepted from clients.
 * - email is derived from linked user/account data and not accepted from clients.
 * - facultyId must stay unique across records.
 */
@Getter
@Setter
@NoArgsConstructor
public class FacultyUpdateRequestDto {

    @NotNull(message = "Faculty id is required")
    @Min(value = 100, message = "Faculty id must be at least 100")
    @Max(value = 999, message = "Faculty id must be at most 999")
    private Integer facultyId;

    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @Size(max = 100, message = "Designation must be at most 100 characters")
    private String designation;

    @Size(max = 15, message = "Phone must be at most 15 characters")
    private String phone;

    private Set<Long> courseIds = new HashSet<>();
}
