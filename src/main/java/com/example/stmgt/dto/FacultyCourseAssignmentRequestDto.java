package com.example.stmgt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - faculty id comes from path/context.
 * - courseId must exist.
 * - Ignore duplicate assignment requests (faculty-course pair already exists).
 */
@Getter
@Setter
@NoArgsConstructor
public class FacultyCourseAssignmentRequestDto {

    @NotNull(message = "Course id is required")
    private Long courseId;
}
