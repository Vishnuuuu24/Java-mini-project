package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.AuthorizationException;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class GradeManagementService {

    private static final Set<String> ALLOWED_GRADES = Set.of("A", "B", "C", "D", "F");

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final GpaService gpaService;

    public GradeManagementService(
        GradeRepository gradeRepository,
        StudentRepository studentRepository,
        CourseRepository courseRepository,
        FacultyRepository facultyRepository,
        GpaService gpaService
    ) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.gpaService = gpaService;
    }

    @Transactional
    public Grade upsertGrade(Long studentId, Long courseId, String gradeValue) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        validateStudentEnrollment(student, course);
        String normalizedGrade = normalizeAndValidateGrade(gradeValue);

        Grade grade = gradeRepository.findByStudentIdAndCourseId(studentId, courseId)
            .orElseGet(() -> createBlankGrade(student, course));

        grade.setGrade(normalizedGrade);
        Grade savedGrade = gradeRepository.save(grade);

        gpaService.recomputeAndPersistGpa(student);
        return savedGrade;
    }

    @Transactional
    public Grade upsertGradeByFaculty(Long facultyUserId, Long studentId, Long courseId, String gradeValue) {
        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
            .orElseThrow(() -> new NotFoundException("Faculty profile not found for user: " + facultyUserId));

        boolean facultyOwnsCourse = faculty.getCourses().stream()
            .anyMatch(course -> course.getId().equals(courseId));

        if (!facultyOwnsCourse) {
            throw new AuthorizationException("Faculty is not assigned to course: " + courseId);
        }

        return upsertGrade(studentId, courseId, gradeValue);
    }

    @Transactional
    public Grade ensureGradePlaceholder(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        validateStudentEnrollment(student, course);

        return gradeRepository.findByStudentIdAndCourseId(studentId, courseId)
            .orElseGet(() -> {
                Grade grade = createBlankGrade(student, course);
                return gradeRepository.save(grade);
            });
    }

    private Grade createBlankGrade(Student student, Course course) {
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setGrade(null);
        return grade;
    }

    private void validateStudentEnrollment(Student student, Course course) {
        boolean enrolled = student.getCourses().stream()
            .anyMatch(enrolledCourse -> enrolledCourse.getId().equals(course.getId()));

        if (!enrolled) {
            throw new ValidationException("Student is not enrolled in selected course");
        }
    }

    private String normalizeAndValidateGrade(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Grade is required");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_GRADES.contains(normalized)) {
            throw new ValidationException("Grade must be one of A, B, C, D, F");
        }

        return normalized;
    }
}
