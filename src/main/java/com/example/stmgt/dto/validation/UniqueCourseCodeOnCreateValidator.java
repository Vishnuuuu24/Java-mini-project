package com.example.stmgt.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueCourseCodeOnCreateValidator implements ConstraintValidator<UniqueCourseCodeOnCreate, String> {

    private final CourseCodeLookupService lookupService;

    public UniqueCourseCodeOnCreateValidator(CourseCodeLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        return !lookupService.existsByCode(value.trim());
    }
}
