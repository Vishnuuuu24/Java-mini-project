package com.example.stmgt.dto.validation;

import com.example.stmgt.dto.UserRegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRegistrationRequestDto> {

    @Override
    public boolean isValid(UserRegistrationRequestDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String password = value.getPassword();
        String confirmPassword = value.getConfirmPassword();

        if (password == null || confirmPassword == null) {
            return true;
        }

        return password.equals(confirmPassword);
    }
}
