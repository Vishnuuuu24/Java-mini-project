package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.enums.AcademicLevel;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.CourseCreateRequestDto;
import com.example.stmgt.dto.CourseUpdateRequestDto;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.service.CourseLifecycleService;
import com.example.stmgt.service.exception.DomainException;
import com.example.stmgt.service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/courses")
public class CoursesController {

    private final SessionAuthHelper sessionAuthHelper;
    private final CourseRepository courseRepository;
    private final CourseLifecycleService courseLifecycleService;

    public CoursesController(
        SessionAuthHelper sessionAuthHelper,
        CourseRepository courseRepository,
        CourseLifecycleService courseLifecycleService
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.courseRepository = courseRepository;
        this.courseLifecycleService = courseLifecycleService;
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

    @ModelAttribute("course_form")
    public CourseCreateRequestDto courseCreateForm() {
        return new CourseCreateRequestDto();
    }

    @ModelAttribute("course_update_form")
    public CourseUpdateRequestDto courseUpdateForm() {
        return new CourseUpdateRequestDto();
    }

    @GetMapping({"", "/"})
    public String courseList(
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        String search = q.trim().toLowerCase();
        List<Course> courses = new ArrayList<>(courseRepository.findAll());

        if (!search.isBlank()) {
            courses = courses.stream()
                .filter(course -> containsIgnoreCase(course.getName(), search)
                    || containsIgnoreCase(course.getCode(), search))
                .collect(Collectors.toList());
        }

        courses.sort(Comparator.comparing(Course::getName, String.CASE_INSENSITIVE_ORDER));
        int totalCourses = courses.size();
        boolean hasMore = false;
        if (showAll.isBlank() && search.isBlank() && courses.size() > 5) {
            courses = courses.subList(0, 5);
            hasMore = totalCourses > 5;
        }

        model.addAttribute("courses", courses);
        model.addAttribute("q", q);
        model.addAttribute("has_more", hasMore);
        model.addAttribute("show_all", showAll);
        model.addAttribute("total_courses", totalCourses);
        return "courses/course_list";
    }

    @PostMapping(value = {"", "/"}, params = "create_course")
    public String createCourse(
        @Valid @ModelAttribute("course_form") CourseCreateRequestDto courseForm,
        BindingResult bindingResult,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes,
        @RequestParam(name = "q", required = false, defaultValue = "") String q,
        @RequestParam(name = "show_all", required = false, defaultValue = "") String showAll
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        if (bindingResult.hasErrors()) {
            return courseList(q, showAll, session, model);
        }

        Course course = new Course();
        course.setName(courseForm.getName().trim());
        course.setCode(courseForm.getCode().trim());
        course.setLevel(courseForm.getLevel());

        Course created;
        try {
            created = courseLifecycleService.saveCourseWithFacultyAssignments(course, courseForm.getFacultyUserIds());
        } catch (DomainException exception) {
            bindingResult.reject("courseCreateFailed", exception.getMessage());
            return courseList(q, showAll, session, model);
        }

        redirectAttributes.addFlashAttribute("success", "Course '" + created.getName() + "' created successfully.");
        return "redirect:/courses/";
    }

    @GetMapping("/{courseId}/delete/")
    public String deleteCoursePage(
        @PathVariable("courseId") Long courseId,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
        model.addAttribute("course", course);
        return "courses/delete_course";
    }

    @PostMapping("/{courseId}/delete/")
    public String deleteCourse(
        @PathVariable("courseId") Long courseId,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        courseLifecycleService.deleteCourse(courseId);
        redirectAttributes.addFlashAttribute("success", "Course deleted successfully.");
        return "redirect:/courses/";
    }

    @GetMapping("/{courseId}/update/")
    public String updateCoursePage(
        @PathVariable("courseId") Long courseId,
        HttpSession session,
        Model model
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        CourseUpdateRequestDto updateForm = new CourseUpdateRequestDto();
        updateForm.setCourseId(course.getId());
        updateForm.setName(course.getName());
        updateForm.setCode(course.getCode());
        updateForm.setLevel(course.getLevel());
        updateForm.setFacultyUserIds(course.getFacultyUsers().stream().map(user -> user.getId()).collect(Collectors.toSet()));

        model.addAttribute("course", course);
        model.addAttribute("course_update_form", updateForm);
        return "courses/update_course";
    }

    @PostMapping("/{courseId}/update/")
    public Object updateCourse(
        @PathVariable("courseId") Long courseId,
        @Valid @ModelAttribute("course_update_form") CourseUpdateRequestDto updateForm,
        BindingResult bindingResult,
        HttpSession session,
        HttpServletRequest request,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        sessionAuthHelper.requireRole(session, UserRole.ADMIN);

        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
        updateForm.setCourseId(courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        if (bindingResult.hasErrors()) {
            if (ajax) {
                return ResponseEntity.badRequest().body(ajaxError(bindingResult.getFieldError() == null
                    ? "Invalid input"
                    : bindingResult.getFieldError().getDefaultMessage()));
            }

            model.addAttribute("course", course);
            return "courses/update_course";
        }

        course.setName(updateForm.getName().trim());
        course.setCode(updateForm.getCode().trim());
        course.setLevel(updateForm.getLevel());

        try {
            courseLifecycleService.saveCourseWithFacultyAssignments(course, updateForm.getFacultyUserIds());
        } catch (DomainException exception) {
            if (ajax) {
                return ResponseEntity.badRequest().body(ajaxError(exception.getMessage()));
            }

            bindingResult.reject("courseUpdateFailed", exception.getMessage());
            model.addAttribute("course", course);
            return "courses/update_course";
        }

        if (ajax) {
            return ResponseEntity.ok(ajaxSuccess());
        }

        redirectAttributes.addFlashAttribute("success", "Course updated successfully.");
        return "redirect:/courses/";
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private Map<String, Object> ajaxSuccess() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        return payload;
    }

    private Map<String, Object> ajaxError(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("error", message);
        return payload;
    }
}
