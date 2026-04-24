package com.example.stmgt.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidLoginCredentialsValidator.class)
public @interface ValidLoginCredentials {

    String message() default "Invalid username or password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
