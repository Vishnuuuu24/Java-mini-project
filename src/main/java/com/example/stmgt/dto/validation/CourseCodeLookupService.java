package com.example.stmgt.dto.validation;

/**
 * Lookup contract used by DTO validators to enforce Django-parity course code uniqueness.
 */
public interface CourseCodeLookupService {

    boolean existsByCode(String code);

    boolean existsByCodeExcludingCourseId(String code, Long excludedCourseId);
}
