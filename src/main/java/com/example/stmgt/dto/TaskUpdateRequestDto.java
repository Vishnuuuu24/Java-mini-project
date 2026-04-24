package com.example.stmgt.dto;

import com.example.stmgt.domain.enums.TaskPriority;
import com.example.stmgt.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * Service-layer validation contract:
 * - Every assignedStudentId must refer to an existing user with role STUDENT.
 * - Preserve createdBy from existing task unless an explicit reassignment policy allows changes.
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskUpdateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Assigned students are required")
    @Size(min = 1, message = "At least one student must be assigned")
    private Set<Long> assignedStudentIds;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;
}
