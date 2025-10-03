package co.com.pragma.api.dto.response;

import java.util.UUID;

public record UserValidationResponse(
        UUID idUser,
        String email,
        String idDocument,
        Double baseSalary,
        String role
) {
}
