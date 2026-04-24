package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Task;
import com.example.stmgt.domain.entity.TaskProgress;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.repository.TaskProgressRepository;
import com.example.stmgt.repository.TaskRepository;
import com.example.stmgt.service.exception.AuthorizationException;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import com.example.stmgt.service.model.TaskProgressSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskProgressService {

    private final TaskRepository taskRepository;
    private final TaskProgressRepository taskProgressRepository;

    public TaskProgressService(TaskRepository taskRepository, TaskProgressRepository taskProgressRepository) {
        this.taskRepository = taskRepository;
        this.taskProgressRepository = taskProgressRepository;
    }

    @Transactional(readOnly = true)
    public TaskProgressSummary getProgressSummary(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        long totalAssignedStudents = task.getAssignedStudents().size();
        long completedCount = taskProgressRepository.countByTaskIdAndStatus(taskId, TaskStatus.COMPLETED);
        long inProgressCount = taskProgressRepository.countByTaskIdAndStatus(taskId, TaskStatus.IN_PROGRESS);
        long explicitPendingCount = taskProgressRepository.countByTaskIdAndStatus(taskId, TaskStatus.PENDING);

        Set<Long> studentsWithProgress = taskProgressRepository.findByTaskId(taskId).stream()
            .map(progress -> progress.getStudent().getId())
            .collect(Collectors.toSet());

        long implicitPendingCount = Math.max(0, totalAssignedStudents - studentsWithProgress.size());
        long pendingCount = explicitPendingCount + implicitPendingCount;

        TaskStatus overallStatus = resolveOverallStatus(totalAssignedStudents, completedCount, inProgressCount);

        return new TaskProgressSummary(
            pendingCount,
            inProgressCount,
            completedCount,
            totalAssignedStudents,
            overallStatus
        );
    }

    @Transactional
    public TaskProgress updateStudentProgress(Long taskId, Long studentUserId, TaskStatus status) {
        if (status == null) {
            throw new ValidationException("Task status is required");
        }

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        boolean assigned = task.getAssignedStudents().stream()
            .anyMatch(student -> student.getId().equals(studentUserId));

        if (!assigned) {
            throw new AuthorizationException("Student is not assigned to this task");
        }

        TaskProgress taskProgress = taskProgressRepository.findByTaskIdAndStudentId(taskId, studentUserId)
            .orElseGet(() -> createTaskProgress(task, studentUserId, status));

        taskProgress.setStatus(status);
        return taskProgressRepository.save(taskProgress);
    }

    @Transactional
    public TaskProgress markTaskCompletedByAssignedStudent(Long taskId, Long studentUserId) {
        TaskProgress progress = updateStudentProgress(taskId, studentUserId, TaskStatus.COMPLETED);

        Task task = progress.getTask();
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        return progress;
    }

    private TaskProgress createTaskProgress(Task task, Long studentUserId, TaskStatus status) {
        TaskProgress progress = new TaskProgress();
        progress.setTask(task);
        progress.setStudent(task.getAssignedStudents().stream()
            .filter(student -> student.getId().equals(studentUserId))
            .findFirst()
            .orElseThrow(() -> new AuthorizationException("Student is not assigned to this task")));
        progress.setStatus(status);
        return progress;
    }

    private TaskStatus resolveOverallStatus(long totalAssignedStudents, long completedCount, long inProgressCount) {
        if (totalAssignedStudents > 0 && completedCount == totalAssignedStudents) {
            return TaskStatus.COMPLETED;
        }
        if (inProgressCount > 0) {
            return TaskStatus.IN_PROGRESS;
        }
        return TaskStatus.PENDING;
    }
}
