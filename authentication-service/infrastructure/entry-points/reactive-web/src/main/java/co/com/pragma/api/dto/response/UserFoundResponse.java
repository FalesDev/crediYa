package co.com.pragma.api.dto.response;

import java.util.UUID;

public record UserFoundResponse(
        UUID idUser,
        String firstName,
        String lastName,
        String email,
        String idDocument,
        Double baseSalary
) {
}
