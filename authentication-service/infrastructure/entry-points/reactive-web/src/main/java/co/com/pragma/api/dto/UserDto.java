package co.com.pragma.api.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String idDocument,
        String phoneNumber,
        UUID idRole,
        Double baseSalary,
        String password
) {
}
