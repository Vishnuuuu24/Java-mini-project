package com.example.stmgt.service;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Task;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.TaskCreateRequestDto;
import com.example.stmgt.dto.TaskUpdateRequestDto;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.TaskRepository;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskLifecycleService {

    private final TaskRepository taskRepository;
    private final CustomUserRepository customUserRepository;
    private final TaskProgressService taskProgressService;

    public TaskLifecycleService(
        TaskRepository taskRepository,
        CustomUserRepository customUserRepository,
        TaskProgressService taskProgressService
    ) {
        this.taskRepository = taskRepository;
        this.customUserRepository = customUserRepository;
        this.taskProgressService = taskProgressService;
    }

    @Transactional
    public Task createTask(TaskCreateRequestDto form, Long createdByUserId) {
        CustomUser createdBy = customUserRepository.findById(createdByUserId)
            .orElseThrow(() -> new NotFoundException("User not found: " + createdByUserId));

        if (createdBy.getRole() != UserRole.ADMIN && createdBy.getRole() != UserRole.FACULTY) {
            throw new ValidationException("Only admin or faculty users can create tasks.");
        }

        List<CustomUser> assignedStudents = resolveAssignedStudents(form.getAssignedStudentIds());

        Task task = new Task();
        task.setTitle(form.getTitle().trim());
        task.setDescription(form.getDescription().trim());
        task.setDueDate(form.getDueDate());
        task.setPriority(form.getPriority());
        task.setStatus(defaultStatus(form.getStatus()));
        task.setCreatedBy(createdBy);
        task.getAssignedStudents().addAll(assignedStudents);
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateRequestDto form) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        List<CustomUser> assignedStudents = resolveAssignedStudents(form.getAssignedStudentIds());

        task.setTitle(form.getTitle().trim());
        task.setDescription(form.getDescription().trim());
        task.setDueDate(form.getDueDate());
        task.setPriority(form.getPriority());
        task.setStatus(defaultStatus(form.getStatus()));
        task.getAssignedStudents().clear();
        task.getAssignedStudents().addAll(assignedStudents);

        return taskRepository.save(task);
    }

    @Transactional
    public Task deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        taskRepository.delete(task);
        return task;
    }

    @Transactional
    public void updateStudentProgress(Long taskId, Long studentUserId, TaskStatus status) {
        taskProgressService.updateStudentProgress(taskId, studentUserId, status);
    }

    @Transactional
    public Task markTaskCompletedByAssignedStudent(Long taskId, Long studentUserId) {
        return taskProgressService.markTaskCompletedByAssignedStudent(taskId, studentUserId).getTask();
    }

    private List<CustomUser> resolveAssignedStudents(Set<Long> assignedStudentIds) {
        if (assignedStudentIds == null || assignedStudentIds.isEmpty()) {
            throw new ValidationException("At least one student must be assigned.");
        }

        List<CustomUser> assignedStudents = customUserRepository.findAllById(assignedStudentIds).stream()
            .filter(user -> user.getRole() == UserRole.STUDENT)
            .collect(Collectors.toList());

        if (assignedStudents.size() != assignedStudentIds.size()) {
            throw new ValidationException("One or more assigned students are invalid.");
        }

        return assignedStudents;
    }

    private TaskStatus defaultStatus(TaskStatus requestedStatus) {
        return requestedStatus == null ? TaskStatus.PENDING : requestedStatus;
    }
}
