package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Task;
import com.example.stmgt.domain.enums.TaskPriority;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.FacultyCourseAssignmentRequestDto;
import com.example.stmgt.dto.FacultyCreateRequestDto;
import com.example.stmgt.dto.FacultyUpdateRequestDto;
import com.example.stmgt.dto.TaskCreateRequestDto;
import com.example.stmgt.dto.TaskUpdateRequestDto;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.repository.TaskRepository;
import com.example.stmgt.service.FacultyLifecycleService;
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
@RequestMapping("/faculty")
public class FacultyController {

    private final SessionAuthHelper sessionAuthHelper;
    private final FacultyRepository facultyRepository;
    private final CustomUserRepository customUserRepository;
    private final TaskRepository taskRepository;
    private final FacultyLifecycleService facultyLifecycleService;
    private final TaskLifecycleService taskLifecycleService;

    public FacultyController(
        SessionAuthHelper sessionAuthHelper,
        FacultyRepository facultyRepository,
        CustomUserRepository customUserRepository,
        TaskRepository taskRepository,
        FacultyLifecycleService facultyLifecycleService,
        TaskLifecycleService taskLifecycleService
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.facultyRepository = facultyRepository;
        this.customUserRepository = customUserRepository;
        this.taskRepository = taskRepository;
        this.facultyLifecycleService = facultyLifecycleService;
        this.taskLifecycleService = taskLifecycleService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(TaskPriority.class, enumEditor(TaskPriority::fromValue));
        binder.registerCustomEditor(TaskStatus.class, enumEditor(TaskStatus::fromValue));
    }

    @ModelAttribute("faculty_form")
    public FacultyCreateRequestDto facultyCreateForm() {
        return new FacultyCreateRequestDto();
    }

    @ModelAttribute("course_form")
    public FacultyCourseAssignmentRequestDto facultyCourseForm() {
        return new FacultyCourseAssignmentRequestDto();
    }

    @ModelAttribute("faculty_update_form")
    public FacultyUpdateRequestDto facultyUpdateForm() {
        return new FacultyUpdateRequestDto();
    }

    @ModelAttribute("form")
    public TaskCreateRequestDto taskCreateForm() {
        return new TaskCreateRequestDto();
    }

    @ModelAttribute("task_update_form")
    public TaskUpdateRequestDto taskUpdateForm() {
        return new TaskUpdateRequestDto();
    }

    @GetMapping("/api/user-email/{userId}/")
    @ResponseBody
    public Map<String, String> getUserEmail(
        @PathVariable("userId") Long userId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        CustomUser user = customUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Map<String, String> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        return payload;
    }

    @GetMapping("/faculty_list/")
    public String facultyList(
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        String search = q.trim().toLowerCase();
        List<Faculty> faculties = new ArrayList<>(facultyRepository.findAll());
        faculties.sort(Comparator.comparing(faculty -> {
            if (faculty.getUser() == null || faculty.getUser().getUsername() == null) {
                return "";
            }
            return faculty.getUser().getUsername().toLowerCase();
        }));

        if (!search.isBlank()) {
            faculties = faculties.stream()
                .filter(faculty -> containsIgnoreCase(faculty.getFacultyId() == null ? "" : String.valueOf(faculty.getFacultyId()), search)
                    || (faculty.getUser() != null && (
                        containsIgnoreCase(faculty.getUser().getUsername(), search)
                        || containsIgnoreCase(faculty.getUser().getFirstName(), search)
                        || containsIgnoreCase(faculty.getUser().getLastName(), search)
                    )))
                .collect(Collectors.toList());
        }

        int totalFaculties = faculties.size();
        boolean hasMore = false;
        if (showAll.isBlank() && search.isBlank() && faculties.size() > 5) {
            faculties = faculties.subList(0, 5);
            hasMore = totalFaculties > 5;
        }

        model.addAttribute("faculties", faculties);
        model.addAttribute("q", q);
        model.addAttribute("total_faculties", totalFaculties);
        model.addAttribute("has_more", hasMore);
        model.addAttribute("show_all", showAll);
        return "faculty/faculty_list";
    }

    @PostMapping(value = "/faculty_list/", params = "create_faculty")
    public String createFaculty(
        @Valid @ModelAttribute("faculty_form") FacultyCreateRequestDto facultyForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            return facultyList(q, showAll, session, model);
        }

        try {
            facultyLifecycleService.createFacultyProfile(
                facultyForm.getUserId(),
                facultyForm.getFacultyId(),
                facultyForm.getDepartment(),
                facultyForm.getDesignation(),
                facultyForm.getEmail(),
                facultyForm.getPhone(),
                facultyForm.getCourseIds()
            );
            redirectAttributes.addFlashAttribute("success", "Faculty created successfully.");
            return "redirect:/faculty/faculty_list/";
        } catch (DomainException exception) {
            bindingResult.reject("createFaculty", exception.getMessage());
            return facultyList(q, showAll, session, model);
        }
    }

    @GetMapping("/{facultyId}/manage/")
    public String manageFaculty(
        @PathVariable("facultyId") Long facultyId,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);
        populateManageFacultyModel(facultyId, model);
        return "faculty/manage_faculty";
    }

    @PostMapping(value = "/{facultyId}/manage/", params = "submit_course")
    public String assignCourse(
        @PathVariable("facultyId") Long facultyId,
        @Valid @ModelAttribute("course_form") FacultyCourseAssignmentRequestDto courseForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            populateManageFacultyModel(facultyId, model);
            return "faculty/manage_faculty";
        }

        try {
            redirectAttributes.addFlashAttribute(
                "success",
                facultyLifecycleService.assignCourseToFaculty(facultyId, courseForm.getCourseId())
            );
            return "redirect:/faculty/" + facultyId + "/manage/";
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/faculty/" + facultyId + "/manage/";
        }
    }

    @PostMapping(value = "/{facultyId}/manage/", params = "update_details")
    public String updateFacultyDetails(
        @PathVariable("facultyId") Long facultyId,
        @Valid @ModelAttribute("faculty_update_form") FacultyUpdateRequestDto updateForm,
        BindingResult bindingResult,
        @RequestParam(name = "first_name", required = false, defaultValue = "") String firstName,
        @RequestParam(name = "last_name", required = false, defaultValue = "") String lastName,
        @RequestParam(name = "email", required = false, defaultValue = "") String email,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            populateManageFacultyModel(facultyId, model);
            return "faculty/manage_faculty";
        }

        try {
            redirectAttributes.addFlashAttribute(
                "success",
                facultyLifecycleService.updateFacultyProfile(
                    facultyId,
                    updateForm.getFacultyId(),
                    updateForm.getDepartment(),
                    updateForm.getDesignation(),
                    updateForm.getPhone(),
                    firstName,
                    lastName,
                    email
                )
            );
            return "redirect:/faculty/" + facultyId + "/manage/";
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/faculty/" + facultyId + "/manage/";
        }
    }

    @PostMapping("/delete/{facultyId}/")
    @ResponseBody
    public Map<String, Object> deleteFaculty(
        @PathVariable("facultyId") Long facultyId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        try {
            facultyLifecycleService.deleteFacultyProfile(facultyId);
            Map<String, Object> payload = successPayload();
            payload.put("message", "Faculty member deleted successfully");
            return payload;
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @RequestMapping("/remove-course/{facultyId}/{courseId}/")
    @ResponseBody
    public Map<String, Object> removeCourse(
        @PathVariable("facultyId") Long facultyId,
        @PathVariable("courseId") Long courseId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        try {
            Map<String, Object> payload = successPayload();
            payload.put("message", facultyLifecycleService.removeCourseFromFaculty(facultyId, courseId));
            return payload;
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @GetMapping("/manage-tasks/")
    public String manageFacultyTasks(HttpSession session, Model model) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY, UserRole.ADMIN);
        Faculty faculty = facultyRepository.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Faculty profile not found for user: " + user.getId()));

        model.addAttribute("faculty", faculty);
        model.addAttribute("courses", faculty.getCourses());
        model.addAttribute("students", customUserRepository.findByRole(UserRole.STUDENT));
        model.addAttribute("tasks", taskRepository.findByCreatedByIdOrderByDueDateAsc(user.getId()));
        return "users/faculty_managetasks";
    }

    @PostMapping(value = "/manage-tasks/", params = "create_task")
    public String createFacultyTask(
        @Valid @ModelAttribute("form") TaskCreateRequestDto createForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            return manageFacultyTasks(session, model);
        }

        Task createdTask;
        try {
            createdTask = taskLifecycleService.createTask(createForm, user.getId());
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/faculty/manage-tasks/";
        }

        redirectAttributes.addFlashAttribute("success", "Task '" + createdTask.getTitle() + "' created successfully.");
        return "redirect:/faculty/manage-tasks/";
    }

    @PostMapping(value = "/manage-tasks/", params = "update_task")
    public String updateFacultyTask(
        @RequestParam("task_id") Long taskId,
        @Valid @ModelAttribute("task_update_form") TaskUpdateRequestDto updateForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            return manageFacultyTasks(session, model);
        }

        Task updatedTask;
        try {
            updatedTask = taskLifecycleService.updateTask(taskId, updateForm);
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/faculty/manage-tasks/";
        }

        redirectAttributes.addFlashAttribute("success", "Task '" + updatedTask.getTitle() + "' updated successfully.");
        return "redirect:/faculty/manage-tasks/";
    }

    @PostMapping("/manage-tasks/{taskId}/delete/")
    @ResponseBody
    public Map<String, Object> deleteFacultyTask(
        @PathVariable("taskId") Long taskId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.FACULTY, UserRole.ADMIN);

        try {
            taskLifecycleService.deleteTask(taskId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    private void populateManageFacultyModel(Long facultyId, Model model) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new NotFoundException("Faculty not found: " + facultyId));

        model.addAttribute("faculty", faculty);
        model.addAttribute("courses", faculty.getCourses());
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
