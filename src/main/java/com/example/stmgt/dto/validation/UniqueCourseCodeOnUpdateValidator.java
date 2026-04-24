package com.example.stmgt.dto.validation;

import com.example.stmgt.dto.CourseUpdateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueCourseCodeOnUpdateValidator
    implements ConstraintValidator<UniqueCourseCodeOnUpdate, CourseUpdateRequestDto> {

    private final CourseCodeLookupService lookupService;

    public UniqueCourseCodeOnUpdateValidator(CourseCodeLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public boolean isValid(CourseUpdateRequestDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.getCode() == null || value.getCode().isBlank()) {
            return true;
        }

        if (value.getCourseId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Course id is required for update uniqueness validation")
                .addPropertyNode("courseId")
                .addConstraintViolation();
            return false;
        }

        boolean exists = lookupService.existsByCodeExcludingCourseId(value.getCode().trim(), value.getCourseId());
        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Course code already exists")
                .addPropertyNode("code")
                .addConstraintViolation();
        }

        return !exists;
    }
}
