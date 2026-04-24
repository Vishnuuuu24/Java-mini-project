package com.example.stmgt.dto;

import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.dto.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - Username must be unique (users_customuser.username).
 * - Apply Django-equivalent password policy checks that require contextual checks
 *   (common password dictionary, user-attribute similarity, etc.) when parity is required.
 */
@Getter
@Setter
@NoArgsConstructor
@PasswordMatches
public class UserRegistrationRequestDto {

    @NotBlank(message = "Username is required")
    @Size(max = 150, message = "Username must be at most 150 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 254, message = "Email must be at most 254 characters")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 150, message = "First name must be at most 150 characters")
    private String firstName;

    @Size(max = 150, message = "Last name must be at most 150 characters")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?!\\d+$).+$", message = "Password cannot be entirely numeric")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, max = 128, message = "Confirm password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?!\\d+$).+$", message = "Confirm password cannot be entirely numeric")
    private String confirmPassword;

    @NotNull(message = "Role is required")
    private UserRole role;
}
