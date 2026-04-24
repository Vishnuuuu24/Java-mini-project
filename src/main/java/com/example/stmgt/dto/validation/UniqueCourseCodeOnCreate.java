package com.example.stmgt.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueCourseCodeOnCreateValidator.class)
public @interface UniqueCourseCodeOnCreate {

    String message() default "Course code already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
