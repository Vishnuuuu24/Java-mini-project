package com.example.stmgt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - studentId and courseId must refer to existing records.
 * - courseId must belong to the selected student's enrolled courses.
 * - Upsert semantics: update if (studentId, courseId) exists, otherwise create.
 */
@Getter
@Setter
@NoArgsConstructor
public class GradeUpsertRequestDto {

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotBlank(message = "Grade is required")
    @Size(max = 2, message = "Grade must be at most 2 characters")
    @Pattern(regexp = "^[ABCDF]$", message = "Grade must be one of A, B, C, D, F")
    private String grade;
}
