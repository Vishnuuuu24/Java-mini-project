package com.example.stmgt.dto;

import com.example.stmgt.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - task id comes from path/context.
 * - Authorization and allowed status transitions are enforced in service layer.
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskStatusUpdateRequestDto {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
