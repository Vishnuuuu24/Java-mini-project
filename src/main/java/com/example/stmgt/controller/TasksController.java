package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Task;
import com.example.stmgt.domain.enums.TaskPriority;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.TaskCreateRequestDto;
import com.example.stmgt.dto.TaskUpdateRequestDto;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.TaskRepository;
import com.example.stmgt.service.TaskLifecycleService;
import com.example.stmgt.service.exception.DomainException;
import com.example.stmgt.service.exception.NotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TasksController {

    private final SessionAuthHelper sessionAuthHelper;
    private final TaskRepository taskRepository;
    private final CustomUserRepository customUserRepository;
    private final TaskLifecycleService taskLifecycleService;

    public TasksController(
        SessionAuthHelper sessionAuthHelper,
        TaskRepository taskRepository,
        CustomUserRepository customUserRepository,
        TaskLifecycleService taskLifecycleService
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.taskRepository = taskRepository;
        this.customUserRepository = customUserRepository;
        this.taskLifecycleService = taskLifecycleService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(TaskPriority.class, enumEditor(TaskPriority::fromValue));
        binder.registerCustomEditor(TaskStatus.class, enumEditor(TaskStatus::fromValue));
    }

    @ModelAttribute("task_form")
    public TaskCreateRequestDto taskCreateForm() {
        return new TaskCreateRequestDto();
    }

    @ModelAttribute("form")
    public TaskUpdateRequestDto taskUpdateForm() {
        return new TaskUpdateRequestDto();
    }

    @GetMapping({"", "/"})
    public String taskList(
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        String search = q.trim().toLowerCase();
        List<Task> tasks = new ArrayList<>(taskRepository.findAll());

        if (!search.isBlank()) {
            tasks = tasks.stream()
                .filter(task -> containsIgnoreCase(task.getTitle(), search)
                    || containsIgnoreCase(task.getDescription(), search))
                .collect(Collectors.toList());
        }

        tasks.sort(Comparator.comparing(task -> task.getTitle().toLowerCase()));

        model.addAttribute("tasks", tasks);
        model.addAttribute("q", q);
        model.addAttribute("students", customUserRepository.findByRole(UserRole.STUDENT));
        return "tasks/task_list";
    }

    @PostMapping(value = {"", "/"}, params = "create_task")
    public String createTask(
        @Valid @ModelAttribute("task_form") TaskCreateRequestDto taskForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(name = "q", required = false, defaultValue = "") String q
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            return taskList(q, session, model);
        }

        Task createdTask;
        try {
            createdTask = taskLifecycleService.createTask(taskForm, user.getId());
        } catch (DomainException exception) {
            bindingResult.reject("taskCreateFailed", exception.getMessage());
            return taskList(q, session, model);
        }

        redirectAttributes.addFlashAttribute("success", "Task '" + createdTask.getTitle() + "' created successfully.");
        return "redirect:/tasks/";
    }

    @GetMapping("/{taskId}/manage/")
    public String manageTask(
        @PathVariable("taskId") Long taskId,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        TaskUpdateRequestDto form = new TaskUpdateRequestDto();
        form.setTitle(task.getTitle());
        form.setDescription(task.getDescription());
        form.setDueDate(task.getDueDate());
        form.setPriority(task.getPriority());
        form.setStatus(task.getStatus());
        form.setAssignedStudentIds(task.getAssignedStudents().stream().map(CustomUser::getId).collect(Collectors.toSet()));

        model.addAttribute("task", task);
        model.addAttribute("form", form);
        model.addAttribute("students", customUserRepository.findByRole(UserRole.STUDENT));
        return "tasks/manage_task";
    }

    @PostMapping(value = "/{taskId}/manage/", params = "update_task")
    public String updateTask(
        @PathVariable("taskId") Long taskId,
        @Valid @ModelAttribute("form") TaskUpdateRequestDto form,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            return renderManageTask(taskId, model);
        }

        Task updatedTask;
        try {
            updatedTask = taskLifecycleService.updateTask(taskId, form);
        } catch (DomainException exception) {
            bindingResult.reject("taskUpdateFailed", exception.getMessage());
            return renderManageTask(taskId, model);
        }

        redirectAttributes.addFlashAttribute("success", "Task '" + updatedTask.getTitle() + "' updated successfully.");
        return "redirect:/tasks/";
    }

    @PostMapping(value = "/{taskId}/manage/", params = "delete_task")
    public String deleteTaskFromManage(
        @PathVariable("taskId") Long taskId,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        Task task = taskLifecycleService.deleteTask(taskId);
        redirectAttributes.addFlashAttribute("success", "Task '" + task.getTitle() + "' deleted successfully.");
        return "redirect:/tasks/";
    }

    @PostMapping("/{taskId}/delete/")
    @ResponseBody
    public Map<String, Object> deleteTask(
        @PathVariable("taskId") Long taskId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        try {
            taskLifecycleService.deleteTask(taskId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @PostMapping("/{taskId}/update-progress/")
    @ResponseBody
    public Map<String, Object> updateTaskProgress(
        @PathVariable("taskId") Long taskId,
        @RequestBody(required = false) Map<String, Object> body,
        HttpSession session
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.STUDENT);

        try {
            if (body == null || !body.containsKey("status")) {
                return errorPayload("Invalid JSON data");
            }

            Object statusValue = body.get("status");
            if (!(statusValue instanceof String statusText) || statusText.isBlank()) {
                return errorPayload("Invalid status");
            }

            TaskStatus status;
            try {
                status = TaskStatus.fromValue(statusText.trim());
            } catch (IllegalArgumentException exception) {
                return errorPayload("Invalid status");
            }

            taskLifecycleService.updateStudentProgress(taskId, user.getId(), status);
            Map<String, Object> payload = successPayload();
            payload.put("message", "Task status updated to " + status.getValue());
            return payload;
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    private String renderManageTask(Long taskId, Model model) {
        model.addAttribute("students", customUserRepository.findByRole(UserRole.STUDENT));
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        model.addAttribute("task", task);
        return "tasks/manage_task";
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private PropertyEditorSupport enumEditor(EnumValueParser parser) {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                String normalized = text.trim();
                try {
                    setValue(parser.parse(normalized));
                } catch (IllegalArgumentException exception) {
                    try {
                        setValue(parser.parse(normalized.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        throw exception;
                    }
                }
            }
        };
    }

    @FunctionalInterface
    private interface EnumValueParser {
        Object parse(String value);
    }

    private Map<String, Object> successPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        return payload;
    }

    private Map<String, Object> errorPayload(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("error", message == null ? "Unknown error" : message);
        return payload;
    }
}
