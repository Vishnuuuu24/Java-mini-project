package com.example.stmgt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - student id comes from path/context.
 * - courseId must exist.
 * - Course level must match the target student's level.
 * - Reject or ignore duplicate student-course assignments.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentCourseAssignmentRequestDto {

    @NotNull(message = "Course id is required")
    private Long courseId;
}
