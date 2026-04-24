package com.example.stmgt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Service-layer validation contract:
 * - task id comes from path/context.
 * - Every assignedStudentId must refer to an existing user with role STUDENT.
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskAssignmentRequestDto {

    @NotNull(message = "Assigned students are required")
    @Size(min = 1, message = "At least one student must be assigned")
    private Set<Long> assignedStudentIds;
}
