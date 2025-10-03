package co.com.pragma.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "LoginRequest", description = "Payload for user authentication")
public record LoginRequest(
        @Schema(description = "User's email", example = "test@test.com")
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Email should be valid"
        )
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "User's password", example = "userPassword")
        @NotBlank(message = "Password is required")
        String password
) {
}

