package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.enums.AcademicLevel;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.StudentCourseAssignmentRequestDto;
import com.example.stmgt.dto.StudentCreateRequestDto;
import com.example.stmgt.dto.StudentUpdateRequestDto;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.GradeManagementService;
import com.example.stmgt.service.StudentCourseService;
import com.example.stmgt.service.StudentProfileService;
import com.example.stmgt.service.StudentReportService;
import com.example.stmgt.service.exception.DomainException;
import com.example.stmgt.service.exception.NotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/students")
public class StudentsController {

    private final SessionAuthHelper sessionAuthHelper;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;
    private final StudentProfileService studentProfileService;
    private final StudentCourseService studentCourseService;
    private final GradeManagementService gradeManagementService;
    private final StudentReportService studentReportService;

    public StudentsController(
        SessionAuthHelper sessionAuthHelper,
        StudentRepository studentRepository,
        CourseRepository courseRepository,
        GradeRepository gradeRepository,
        StudentProfileService studentProfileService,
        StudentCourseService studentCourseService,
        GradeManagementService gradeManagementService,
        StudentReportService studentReportService
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
        this.studentProfileService = studentProfileService;
        this.studentCourseService = studentCourseService;
        this.gradeManagementService = gradeManagementService;
        this.studentReportService = studentReportService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(AcademicLevel.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                setValue(AcademicLevel.fromValue(text.trim().toUpperCase()));
            }
        });
    }

    @ModelAttribute("student_form")
    public StudentCreateRequestDto studentCreateForm() {
        return new StudentCreateRequestDto();
    }

    @ModelAttribute("student_update_form")
    public StudentUpdateRequestDto studentUpdateForm() {
        return new StudentUpdateRequestDto();
    }

    @ModelAttribute("course_form")
    public StudentCourseAssignmentRequestDto courseAssignmentForm() {
        return new StudentCourseAssignmentRequestDto();
    }

    @ModelAttribute("grade_form")
    public GradeSubmissionForm gradeSubmissionForm() {
        return new GradeSubmissionForm();
    }

    @GetMapping({"", "/", "/students/"})
    public String studentList(
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        String searchQuery = q.trim().toLowerCase();
        List<Student> students = new ArrayList<>(studentRepository.findAll());
        students.sort(Comparator.comparing(student -> {
            if (student.getUser() == null || student.getUser().getUsername() == null) {
                return "";
            }
            return student.getUser().getUsername().toLowerCase();
        }));

        if (!searchQuery.isBlank()) {
            students = students.stream()
                .filter(student -> containsIgnoreCase(student.getRegisterNo(), searchQuery)
                    || (student.getUser() != null && (
                        containsIgnoreCase(student.getUser().getUsername(), searchQuery)
                        || containsIgnoreCase(student.getUser().getFirstName(), searchQuery)
                        || containsIgnoreCase(student.getUser().getLastName(), searchQuery)
                    )))
                .collect(Collectors.toList());
        }

        int totalStudents = students.size();
        boolean hasMore = false;
        if (showAll.isBlank() && searchQuery.isBlank() && students.size() > 5) {
            students = students.subList(0, 5);
            hasMore = totalStudents > 5;
        }

        model.addAttribute("students", students);
        model.addAttribute("q", q);
        model.addAttribute("has_more", hasMore);
        model.addAttribute("total_students", totalStudents);
        model.addAttribute("show_all", showAll);
        return "students/student_list";
    }

    @PostMapping(value = {"", "/", "/students/"}, params = "create_student")
    public String createStudent(
        @Valid @ModelAttribute("student_form") StudentCreateRequestDto studentForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            return studentList(q, showAll, session, model);
        }

        try {
            studentProfileService.createStudentProfile(
                studentForm.getUserId(),
                studentForm.getRegisterNo(),
                studentForm.getDepartment(),
                studentForm.getAttendance(),
                studentForm.getLevel(),
                studentForm.getCourseIds()
            );
            redirectAttributes.addFlashAttribute("success", "Student created successfully.");
            return "redirect:/students/";
        } catch (DomainException exception) {
            bindingResult.reject("createStudent", exception.getMessage());
            return studentList(q, showAll, session, model);
        }
    }

    @GetMapping("/{studentId}/")
    public String manageStudent(
        @PathVariable("studentId") Long studentId,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);
        populateManageStudentModel(studentId, model);
        return "students/manage_student";
    }

    @PostMapping(value = "/{studentId}/", params = "update_details")
    public String updateStudentDetails(
        @PathVariable("studentId") Long studentId,
        @Valid @ModelAttribute("student_update_form") StudentUpdateRequestDto studentUpdateForm,
        BindingResult bindingResult,
        @RequestParam(name = "first_name", required = false, defaultValue = "") String firstName,
        @RequestParam(name = "last_name", required = false, defaultValue = "") String lastName,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            populateManageStudentModel(studentId, model);
            return "students/manage_student";
        }

        Student updated;
        try {
            updated = studentProfileService.updateStudentProfile(
                studentId,
                firstName,
                lastName,
                studentUpdateForm.getRegisterNo(),
                studentUpdateForm.getDepartment(),
                studentUpdateForm.getAttendance(),
                studentUpdateForm.getLevel()
            );
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/students/" + studentId + "/";
        }

        redirectAttributes.addFlashAttribute(
            "success",
            "Details for " + updated.getUser().getUsername() + " have been updated."
        );
        return "redirect:/students/" + studentId + "/";
    }

    @PostMapping(value = "/{studentId}/", params = "submit_course")
    public String assignCourse(
        @PathVariable("studentId") Long studentId,
        @Valid @ModelAttribute("course_form") StudentCourseAssignmentRequestDto courseForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            populateManageStudentModel(studentId, model);
            return "students/manage_student";
        }

        try {
            studentCourseService.assignCourseToStudent(studentId, courseForm.getCourseId());
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/students/" + studentId + "/";
        }

        Course course = courseRepository.findById(courseForm.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseForm.getCourseId()));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        redirectAttributes.addFlashAttribute(
            "success",
            "Course '" + course.getName() + "' assigned to " + student.getUser().getUsername() + " successfully."
        );
        return "redirect:/students/" + studentId + "/";
    }

    @PostMapping(value = "/{studentId}/", params = "submit_grade")
    public String saveGrade(
        @PathVariable("studentId") Long studentId,
        @Valid @ModelAttribute("grade_form") GradeSubmissionForm gradeForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        if (bindingResult.hasErrors()) {
            populateManageStudentModel(studentId, model);
            return "students/manage_student";
        }

        try {
            gradeManagementService.upsertGrade(studentId, gradeForm.getCourseId(), gradeForm.getGrade());
        } catch (DomainException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/students/" + studentId + "/";
        }

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        redirectAttributes.addFlashAttribute(
            "success",
            "Grade for " + student.getUser().getUsername() + " saved."
        );
        return "redirect:/students/" + studentId + "/";
    }

    @PostMapping("/{studentId}/delete/")
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

    @PostMapping("/{studentId}/remove_course/{courseId}/")
    @ResponseBody
    public Map<String, Object> removeCourse(
        @PathVariable("studentId") Long studentId,
        @PathVariable("courseId") Long courseId,
        HttpSession session
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN, UserRole.FACULTY);

        try {
            studentCourseService.removeCourseFromStudent(studentId, courseId);
            return successPayload();
        } catch (DomainException exception) {
            return errorPayload(exception.getMessage());
        } catch (Exception exception) {
            return errorPayload(exception.getMessage());
        }
    }

    @GetMapping("/export/csv/")
    public ResponseEntity<ByteArrayResource> exportGradesCsvForAll(HttpSession session) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        String csv = studentReportService.generateCsvReport(null);
        String filename = "grades_all.csv";
        return download(csv.getBytes(StandardCharsets.UTF_8), "text/csv", filename);
    }

    @GetMapping("/{studentId}/export/csv/")
    public ResponseEntity<ByteArrayResource> exportGradesCsvForStudent(
        @PathVariable("studentId") Long studentId,
        HttpSession session
    ) {
        CustomUser user = sessionAuthHelper.requireAuthenticated(session);
        if (user.getRole() != UserRole.ADMIN && !isStudentOwner(user, studentId)) {
            return ResponseEntity.status(401).build();
        }

        String csv = studentReportService.generateCsvReport(studentId);
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
        String filename = "grades_" + student.getRegisterNo() + ".csv";
        return download(csv.getBytes(StandardCharsets.UTF_8), "text/csv", filename);
    }

    @GetMapping("/export/pdf/")
    public ResponseEntity<ByteArrayResource> exportGradesPdfForAll(HttpSession session) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        String report = studentReportService.generatePlainTextGradeReport(null);
        return download(report.getBytes(StandardCharsets.UTF_8), "application/pdf", "grades_all.pdf");
    }

    @GetMapping("/{studentId}/export/pdf/")
    public ResponseEntity<ByteArrayResource> exportGradesPdfForStudent(
        @PathVariable("studentId") Long studentId,
        HttpSession session
    ) {
        CustomUser user = sessionAuthHelper.requireAuthenticated(session);
        if (user.getRole() != UserRole.ADMIN && !isStudentOwner(user, studentId)) {
            return ResponseEntity.status(401).build();
        }

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
        String report = studentReportService.generatePlainTextGradeReport(studentId);
        return download(
            report.getBytes(StandardCharsets.UTF_8),
            "application/pdf",
            "grades_" + student.getRegisterNo() + ".pdf"
        );
    }

    private void populateManageStudentModel(Long studentId, Model model) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        List<Grade> grades = gradeRepository.findByStudentId(student.getId());
        List<Course> courses = student.getCourses().stream().collect(Collectors.toList());
        double gpa = student.getGpa() == null ? 0.0 : student.getGpa();

        model.addAttribute("student", student);
        model.addAttribute("grades", grades);
        model.addAttribute("courses", courses);
        model.addAttribute("gpa", gpa);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private boolean isStudentOwner(CustomUser user, Long studentId) {
        return studentRepository.findById(studentId)
            .map(student -> student.getUser() != null && Objects.equals(student.getUser().getId(), user.getId()))
            .orElse(false);
    }

    private ResponseEntity<ByteArrayResource> download(byte[] content, String mimeType, String filename) {
        ByteArrayResource resource = new ByteArrayResource(content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(content.length);
        headers.setContentType(MediaType.parseMediaType(mimeType));
        return ResponseEntity.ok().headers(headers).body(resource);
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

    public static class GradeSubmissionForm {

        @NotNull(message = "Course id is required")
        private Long courseId;

        @NotBlank(message = "Grade is required")
        @Size(max = 2, message = "Grade must be at most 2 characters")
        @Pattern(regexp = "^[ABCDF]$", message = "Grade must be one of A, B, C, D, F")
        private String grade;

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade == null ? null : grade.trim().toUpperCase();
        }
    }
}
