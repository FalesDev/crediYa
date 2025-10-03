package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserValidationRequest(
        @NotBlank(message = "IdDocument is required")
        @Pattern(
                regexp = "^\\d{8}$",
                message = "IdDocument must contain exactly 8 digits"
        )
        String idDocument
) {
}
