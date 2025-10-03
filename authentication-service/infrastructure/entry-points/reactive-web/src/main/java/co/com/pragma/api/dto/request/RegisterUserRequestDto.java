package co.com.pragma.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "RegisterUserRequestDto", description = "Request body for registering a new user in the system")
public record RegisterUserRequestDto(
        @Schema(
                description = "User's first name",
                example = "Fabricio"
        )
        @NotBlank(message = "FirstName is required")
        String firstName,

        @Schema(
                description = "User's last name",
                example = "Rodriguez"
        )
        @NotBlank(message = "LastName is required")
        String lastName,

        @Schema(
                description = "Valid email address of the user",
                example = "fabricio@gmail.com"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Email should be valid"
        )
        @NotBlank(message = "Email is required")
        String email,

        @Schema(
                description = "Identification document number (exactly 8 digits)",
                example = "12345678"
        )
        @NotBlank(message = "IdDocument is required")
        @Pattern(
                regexp = "^\\d{8}$",
                message = "IdDocument must contain exactly 8 digits"
        )
        String idDocument,

        @Schema(
                description = "User's phone number",
                example = "987654321"
        )
        @NotBlank(message = "PhoneNumber is required")
        String phoneNumber,

        @Schema(
                description = "Base salary of the user",
                example = "2500.50"
        )
        @NotNull(message = "BaseSalary is required")
        @Min(value = 0, message = "BaseSalary must be greater or equal to 0")
        @Max(value = 15000000, message = "BaseSalary must be less than or equal to 15,000,000")
        Double baseSalary,

        @Schema(
                description = "Password for the account",
                example = "userPassword"
        )
        @NotBlank(message = "Password is required")
        String password
){
}
