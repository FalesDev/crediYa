package co.com.pragma.api.dto.response;

public record AuthResponse(
        String accessToken,
        long expiresIn
) {
}
