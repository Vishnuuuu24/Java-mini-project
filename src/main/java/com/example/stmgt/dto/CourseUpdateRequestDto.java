package com.example.stmgt.dto;

import com.example.stmgt.domain.enums.AcademicLevel;
import com.example.stmgt.dto.validation.UniqueCourseCodeOnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Service-layer validation contract:
 * - code must be unique excluding the current course id (update path of clean_code).
 * - Every facultyUserId must exist and have role FACULTY.
 */
@Getter
@Setter
@NoArgsConstructor
@UniqueCourseCodeOnUpdate
public class CourseUpdateRequestDto {

    @NotNull(message = "Course id is required for update")
    private Long courseId;

    @NotBlank(message = "Course name is required")
    @Size(max = 100, message = "Course name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Course code is required")
    @Size(max = 10, message = "Course code must be at most 10 characters")
    private String code;

    @NotNull(message = "Level is required")
    private AcademicLevel level;

    private Set<Long> facultyUserIds = new HashSet<>();
}
