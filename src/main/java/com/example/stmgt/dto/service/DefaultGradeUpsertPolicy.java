package com.example.stmgt.dto.service;

import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.dto.GradeUpsertRequestDto;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Enforces Django-equivalent grade upsert semantics:
 * - Validate student/course existence.
 * - Validate student-course membership.
 * - Update existing grade for (studentId, courseId) or create a new one.
 */
@Service
public class DefaultGradeUpsertPolicy implements GradeUpsertPolicy {

    private static final Set<String> ALLOWED_GRADES = Set.of("A", "B", "C", "D", "F");

    private final GradeUpsertLookupService lookupService;

    public DefaultGradeUpsertPolicy(GradeUpsertLookupService lookupService) {
        this.lookupService = Objects.requireNonNull(lookupService, "lookupService must not be null");
    }

    @Override
    public GradeUpsertOutcome upsert(GradeUpsertRequestDto request) {
        Objects.requireNonNull(request, "request must not be null");

        Long studentId = requireNonNull(request.getStudentId(), "Student id is required");
        Long courseId = requireNonNull(request.getCourseId(), "Course id is required");
        String normalizedGrade = normalizeAndValidateGrade(request.getGrade());

        if (!lookupService.studentExists(studentId)) {
            throw new GradeUpsertPolicyException("Student not found: " + studentId);
        }

        if (!lookupService.courseExists(courseId)) {
            throw new GradeUpsertPolicyException("Course not found: " + courseId);
        }

        if (!lookupService.isStudentEnrolledInCourse(studentId, courseId)) {
            throw new GradeUpsertPolicyException("Student is not enrolled in the selected course");
        }

        Optional<Grade> existingGrade = lookupService.findByStudentIdAndCourseId(studentId, courseId);
        Grade grade = existingGrade.orElseGet(() -> createGrade(studentId, courseId));
        grade.setGrade(normalizedGrade);

        Grade persisted = lookupService.save(grade);
        return existingGrade.isPresent()
            ? GradeUpsertOutcome.updated(persisted)
            : GradeUpsertOutcome.created(persisted);
    }

    private Grade createGrade(Long studentId, Long courseId) {
        Grade grade = new Grade();
        grade.setStudent(lookupService.getStudentReference(studentId));
        grade.setCourse(lookupService.getCourseReference(courseId));
        return grade;
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new GradeUpsertPolicyException(message);
        }
        return value;
    }

    private String normalizeAndValidateGrade(String grade) {
        if (grade == null || grade.isBlank()) {
            throw new GradeUpsertPolicyException("Grade is required");
        }

        String normalized = grade.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_GRADES.contains(normalized)) {
            throw new GradeUpsertPolicyException("Grade must be one of A, B, C, D, F");
        }

        return normalized;
    }
}
