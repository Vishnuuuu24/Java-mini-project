package com.example.stmgt.service;

import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.dto.TaskCreateRequestDto;
import com.example.stmgt.dto.TaskUpdateRequestDto;
import org.springframework.stereotype.Service;

@Service
public class TaskCommandOrchestrationService {

    private final TaskLifecycleService taskLifecycleService;

    public TaskCommandOrchestrationService(TaskLifecycleService taskLifecycleService) {
        this.taskLifecycleService = taskLifecycleService;
    }

    public String createTask(TaskCreateRequestDto form, Long createdByUserId) {
        return "Task '" + taskLifecycleService.createTask(form, createdByUserId).getTitle() + "' created successfully.";
    }

    public String updateTask(Long taskId, TaskUpdateRequestDto form) {
        return "Task '" + taskLifecycleService.updateTask(taskId, form).getTitle() + "' updated successfully.";
    }

    public String deleteTask(Long taskId) {
        return "Task '" + taskLifecycleService.deleteTask(taskId).getTitle() + "' deleted successfully.";
    }

    public String markTaskCompletedByAssignedStudent(Long taskId, Long studentUserId) {
        return "Task '" + taskLifecycleService.markTaskCompletedByAssignedStudent(taskId, studentUserId).getTitle()
            + "' marked as completed.";
    }

    public String updateStudentProgress(Long taskId, Long studentUserId, TaskStatus status) {
        taskLifecycleService.updateStudentProgress(taskId, studentUserId, status);
        return "Task status updated to " + status.getValue();
    }
}
