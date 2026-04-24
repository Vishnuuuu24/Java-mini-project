package com.example.stmgt.dto.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;

import java.util.Optional;

/**
 * Data access contract used by grade upsert policy.
 */
public interface GradeUpsertLookupService {

    boolean studentExists(Long studentId);

    boolean courseExists(Long courseId);

    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);

    Optional<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Student getStudentReference(Long studentId);

    Course getCourseReference(Long courseId);

    Grade save(Grade grade);
}
