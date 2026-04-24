package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.enums.TaskPriority;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.FacultyCreateRequestDto;
import com.example.stmgt.dto.TaskCreateRequestDto;
import com.example.stmgt.dto.UserLoginRequestDto;
import com.example.stmgt.dto.UserRegistrationRequestDto;
import com.example.stmgt.dto.validation.DjangoPasswordVerifier;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.CourseLifecycleService;
import com.example.stmgt.service.DashboardService;
import com.example.stmgt.service.FacultyLifecycleService;
import com.example.stmgt.service.GradeManagementService;
import com.example.stmgt.service.StudentProfileService;
import com.example.stmgt.service.TaskCommandOrchestrationService;
import com.example.stmgt.service.UserLifecycleService;
import com.example.stmgt.service.exception.DomainException;
import com.example.stmgt.service.exception.AuthorizationException;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.model.AdminSummary;
import com.example.stmgt.service.model.FacultyDashboardContext;
import com.example.stmgt.service.model.StudentDashboardContext;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UsersController {

    private final SessionAuthHelper sessionAuthHelper;
    private final DashboardService dashboardService;
    private final CustomUserRepository customUserRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;
    private final FacultyLifecycleService facultyLifecycleService;
    private final CourseLifecycleService courseLifecycleService;
    private final GradeManagementService gradeManagementService;
    private final TaskCommandOrchestrationService taskCommandOrchestrationService;
    private final StudentProfileService studentProfileService;
    private final UserLifecycleService userLifecycleService;
    private final DjangoPasswordVerifier passwordVerifier;
    private final DjangoPasswordHasher passwordHasher;

    public UsersController(
        SessionAuthHelper sessionAuthHelper,
        DashboardService dashboardService,
        CustomUserRepository customUserRepository,
        StudentRepository studentRepository,
        FacultyRepository facultyRepository,
        CourseRepository courseRepository,
        GradeRepository gradeRepository,
        FacultyLifecycleService facultyLifecycleService,
        CourseLifecycleService courseLifecycleService,
        GradeManagementService gradeManagementService,
        TaskCommandOrchestrationService taskCommandOrchestrationService,
        StudentProfileService studentProfileService,
        UserLifecycleService userLifecycleService,
        DjangoPasswordVerifier passwordVerifier,
        DjangoPasswordHasher passwordHasher
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.dashboardService = dashboardService;
        this.customUserRepository = customUserRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
        this.facultyLifecycleService = facultyLifecycleService;
        this.courseLifecycleService = courseLifecycleService;
        this.gradeManagementService = gradeManagementService;
        this.taskCommandOrchestrationService = taskCommandOrchestrationService;
        this.studentProfileService = studentProfileService;
        this.userLifecycleService = userLifecycleService;
        this.passwordVerifier = passwordVerifier;
        this.passwordHasher = passwordHasher;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UserRole.class, enumEditor(UserRole::fromValue));
        binder.registerCustomEditor(TaskPriority.class, enumEditor(TaskPriority::fromValue));
        binder.registerCustomEditor(TaskStatus.class, enumEditor(TaskStatus::fromValue));
    }

    @ModelAttribute("form")
    public UserLoginRequestDto loginForm() {
        return new UserLoginRequestDto();
    }

    @ModelAttribute("user_form")
    public UserRegistrationRequestDto userRegistrationForm() {
        return new UserRegistrationRequestDto();
    }

    @ModelAttribute("faculty_form")
    public FacultyCreateRequestDto facultyCreateForm() {
        return new FacultyCreateRequestDto();
    }

    @ModelAttribute("task_form")
    public TaskCreateRequestDto taskCreateForm() {
        return new TaskCreateRequestDto();
    }

    @ModelAttribute("taskCompletionForm")
    public TaskCompletionForm taskCompletionForm() {
        return new TaskCompletionForm();
    }

    @ModelAttribute("assignGradeForm")
    public AssignGradeForm assignGradeForm() {
        return new AssignGradeForm();
    }

    @GetMapping({"", "/"})
    public String homeRedirect() {
        return "redirect:/users/login/";
    }

    @GetMapping("/login/")
    public String loginPage() {
        return "users/login";
    }

    @PostMapping("/login/")
    public String login(
        @Valid @ModelAttribute("form") UserLoginRequestDto form,
        BindingResult bindingResult,
        HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "users/login";
        }

        String normalizedUsername = form.getUsername() == null ? "" : form.getUsername().trim();
        CustomUser user = customUserRepository.findByUsername(normalizedUsername).orElse(null);
        boolean invalidCredentials = user == null
            || !user.isActive()
            || !passwordVerifier.matches(form.getPassword(), user.getPassword());

        if (invalidCredentials) {
            bindingResult.reject("invalidCredentials", "Invalid username or password");
            return "users/login";
        }

        sessionAuthHelper.signIn(session, user);

        if (user.getRole() == UserRole.ADMIN) {
            return "redirect:/users/admin/dashboard/";
        }
        if (user.getRole() == UserRole.FACULTY) {
            return "redirect:/users/faculty/dashboard/";
        }

        return "redirect:/users/student/dashboard/";
    }

    @RequestMapping("/logout/")
    public String logout(HttpSession session) {
        sessionAuthHelper.signOut(session);
        return "redirect:/users/login/";
    }

    @GetMapping("/admin/dashboard/")
    public String adminDashboard(HttpSession session, Model model) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);
        populateAdminDashboardModel(model);
        return "users/admin_dashboard";
    }

    @PostMapping(value = "/admin/dashboard/", params = "create_user")
    public String createUser(
        @Valid @ModelAttribute("user_form") UserRegistrationRequestDto userForm,
        BindingResult bindingResult,
        @ModelAttribute("faculty_form") FacultyCreateRequestDto facultyForm,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            populateAdminDashboardModel(model);
            return "users/admin_dashboard";
        }

        try {
            CustomUser createdUser = userLifecycleService.createUserAccount(
                userForm.getUsername(),
                userForm.getEmail(),
                userForm.getFirstName(),
                userForm.getLastName(),
                userForm.getRole(),
                passwordHasher.hash(userForm.getPassword())
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "User '" + createdUser.getUsername() + "' created successfully."
            );
            return "redirect:/users/admin/dashboard/";
        } catch (DomainException exception) {
            bindingResult.reject("createUser", exception.getMessage());
            populateAdminDashboardModel(model);
            return "users/admin_dashboard";
        }
    }

    @PostMapping(value = "/admin/dashboard/", params = "create_faculty")
    public String createFaculty(
        @Valid @ModelAttribute("faculty_form") FacultyCreateRequestDto facultyForm,
        BindingResult bindingResult,
        @ModelAttribute("user_form") UserRegistrationRequestDto userForm,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            populateAdminDashboardModel(model);
            return "users/admin_dashboard";
        }

        try {
            Faculty savedFaculty = facultyLifecycleService.createFacultyProfile(
                facultyForm.getUserId(),
                facultyForm.getFacultyId(),
                facultyForm.getDepartment(),
                facultyForm.getDesignation(),
                facultyForm.getEmail(),
                facultyForm.getPhone(),
                facultyForm.getCourseIds()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Faculty profile created successfully for '" + savedFaculty.getUser().getUsername() + "'."
            );
            return "redirect:/users/admin/dashboard/";
        } catch (DomainException exception) {
            bindingResult.reject("createFaculty", exception.getMessage());
            populateAdminDashboardModel(model);
            return "users/admin_dashboard";
        }
    }

    @GetMapping("/faculty/dashboard/")
    public String facultyDashboard(HttpSession session, Model model) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY);
        populateFacultyDashboardModel(user, model);
        return "users/faculty_dashboard";
    }

    @PostMapping(value = "/faculty/dashboard/", params = "create_task")
    public String createFacultyTask(
        @Valid @ModelAttribute("task_form") TaskCreateRequestDto taskForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", firstBindingError(bindingResult));
            populateFacultyDashboardModel(user, model);
            return "users/faculty_dashboard";
        }

        try {
            redirectAttributes.addFlashAttribute("success", taskCommandOrchestrationService.createTask(taskForm, user.getId()));
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users/faculty/dashboard/";
    }

    @GetMapping("/student/dashboard/")
    public String studentDashboard(HttpSession session, Model model) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.STUDENT);
        populateStudentDashboardModel(user, model);
        return "users/student_dashboard";
    }

    @PostMapping(value = "/student/dashboard/", params = "complete_task_id")
    public String completeStudentTask(
        @Valid @ModelAttribute("taskCompletionForm") TaskCompletionForm taskCompletionForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.STUDENT);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", firstBindingError(bindingResult));
            populateStudentDashboardModel(user, model);
            return "users/student_dashboard";
        }

        try {
            redirectAttributes.addFlashAttribute(
                "success",
                taskCommandOrchestrationService.markTaskCompletedByAssignedStudent(
                    taskCompletionForm.getComplete_task_id(),
                    user.getId()
                )
            );
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users/student/dashboard/";
    }

    @PostMapping("/students/{studentId}/delete/")
    @ResponseBody
    public Map<String, Object> deleteStudent(
        @PathVariable("studentId") Long studentId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        try {
            studentProfileService.deleteStudentProfile(studentId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @PostMapping("/faculty/{facultyId}/delete/")
    @ResponseBody
    public Map<String, Object> deleteFaculty(
        @PathVariable("facultyId") Long facultyId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        try {
            facultyLifecycleService.deleteFacultyProfile(facultyId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/delete/")
    @ResponseBody
    public Map<String, Object> deleteCourse(
        @PathVariable("courseId") Long courseId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        try {
            courseLifecycleService.deleteCourse(courseId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @GetMapping("/student/grades/")
    public String studentGrades(HttpSession session, Model model) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.STUDENT);

        Student student = studentRepository.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Student profile not found for user: " + user.getId()));

        model.addAttribute("grades", gradeRepository.findByStudentId(student.getId()));
        model.addAttribute("gpa", student.getGpa() == null ? 0.0 : student.getGpa());
        return "students/student_grades";
    }

    @GetMapping("/faculty/courses/{courseId}/grades/")
    public String facultyManageGrades(
        @PathVariable("courseId") Long courseId,
        HttpSession session,
        Model model
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY);

        Faculty faculty = facultyRepository.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Faculty profile not found for user: " + user.getId()));

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        boolean assigned = faculty.getCourses().stream().anyMatch(it -> it.getId().equals(courseId));
        if (!assigned) {
            throw new AuthorizationException("You are not assigned to this course.");
        }

        List<Grade> pendingGrades = gradeRepository.findByCourseId(courseId).stream()
            .filter(grade -> grade.getGrade() == null)
            .collect(Collectors.toList());

        model.addAttribute("course", course);
        model.addAttribute("pending_grades", pendingGrades);
        return "users/faculty_managegradeslist";
    }

    @PostMapping("/faculty/grades/{gradeId}/assign/")
    @ResponseBody
    public Map<String, Object> assignGrade(
        @PathVariable("gradeId") Long gradeId,
        @Valid @ModelAttribute("assignGradeForm") AssignGradeForm assignGradeForm,
        BindingResult bindingResult,
        HttpSession session
    ) {
        CustomUser user = sessionAuthHelper.requireRole(session, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            return errorPayload(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new NotFoundException("Grade not found: " + gradeId));

            gradeManagementService.upsertGradeByFaculty(
                user.getId(),
                grade.getStudent().getId(),
                grade.getCourse().getId(),
                assignGradeForm.getGrade()
            );
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    private void populateAdminDashboardModel(Model model) {
        AdminSummary summary = dashboardService.getAdminSummary();
        model.addAttribute("total_students", summary.getTotalStudents());
        model.addAttribute("total_faculty", summary.getTotalFaculty());
        model.addAttribute("total_tasks", summary.getTotalTasks());
        model.addAttribute("total_courses", summary.getTotalCourses());
    }

    private void populateFacultyDashboardModel(CustomUser user, Model model) {
        FacultyDashboardContext context = dashboardService.getFacultyDashboardContext(user.getId());

        model.addAttribute("faculty", context.getFaculty());
        model.addAttribute("courses", context.getCourses());
        model.addAttribute("students_count", context.getStudentsCount());
        model.addAttribute("tasks", context.getTasks());
        model.addAttribute("pending_grades", context.getPendingGrades());
        model.addAttribute("pending_grades_list", context.getPendingGradesList());
        model.addAttribute("recent_tasks", context.getRecentTasks());
        model.addAttribute("faculty_name", context.getFacultyName());
        model.addAttribute("pending_grades_by_course_id", context.getPendingGradesByCourseId());

        List<Course> coursesWithPendingGrades = context.getCourses().stream()
            .filter(course -> context.getPendingGradesByCourseId().containsKey(course.getId()))
            .collect(Collectors.toList());

        model.addAttribute("courses_with_pending_grades", coursesWithPendingGrades);
        model.addAttribute("students", customUserRepository.findByRole(UserRole.STUDENT));
    }

    private void populateStudentDashboardModel(CustomUser user, Model model) {
        StudentDashboardContext context = dashboardService.getStudentDashboardContext(user.getId());

        model.addAttribute("student", context.getStudent());
        model.addAttribute("grades", context.getGrades());
        model.addAttribute("courses", context.getCourses());
        model.addAttribute("gpa", context.getGpa());
        model.addAttribute("tasks", context.getTasks());
        model.addAttribute("pending_tasks", context.getPendingTasks());
        model.addAttribute("completed_tasks", context.getCompletedTasks());
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

    private String firstBindingError(BindingResult bindingResult) {
        return bindingResult.getFieldError() == null
            ? "Invalid input."
            : bindingResult.getFieldError().getDefaultMessage();
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

    public static class TaskCompletionForm {

        @NotNull(message = "Task id is required")
        private Long complete_task_id;

        public Long getComplete_task_id() {
            return complete_task_id;
        }

        public void setComplete_task_id(Long complete_task_id) {
            this.complete_task_id = complete_task_id;
        }
    }

    public static class AssignGradeForm {

        @NotBlank(message = "Grade is required")
        @Size(max = 2, message = "Grade must be at most 2 characters")
        @Pattern(regexp = "^[ABCDF]$", message = "Grade must be one of A, B, C, D, F")
        private String grade;

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade == null ? null : grade.trim().toUpperCase();
        }
    }
}
