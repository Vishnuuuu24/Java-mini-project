package com.example.stmgt.dto;

import com.example.stmgt.dto.validation.ValidLoginCredentials;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Service-layer validation contract:
 * - Authenticate username/password against the configured authentication provider.
 * - Return the same generic authentication error for invalid username or password.
 */
@Getter
@Setter
@NoArgsConstructor
@ValidLoginCredentials
public class UserLoginRequestDto {

    @NotBlank(message = "Username is required")
    @Size(max = 150, message = "Username must be at most 150 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 128, message = "Password must be at most 128 characters")
    private String password;
}
