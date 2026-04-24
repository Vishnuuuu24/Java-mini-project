package com.example.stmgt.dto.validation;

import com.example.stmgt.dto.UserLoginRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidLoginCredentialsValidator
    implements ConstraintValidator<ValidLoginCredentials, UserLoginRequestDto> {

    private final LoginAuthenticationService authenticationService;

    public ValidLoginCredentialsValidator(LoginAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean isValid(UserLoginRequestDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String username = value.getUsername() == null ? "" : value.getUsername().trim();
        String password = value.getPassword() == null ? "" : value.getPassword();
        return authenticationService.authenticate(username, password);
    }
}
