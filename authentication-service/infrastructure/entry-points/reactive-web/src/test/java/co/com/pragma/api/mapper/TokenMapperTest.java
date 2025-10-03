package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.AuthResponse;
import co.com.pragma.model.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TokenMapperTest {

    private TokenMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(TokenMapper.class);
    }

    @Test
    @DisplayName("Should map Token to AuthResponse successfully")
    void shouldMapTokenToAuthResponse() {
        Token token = new Token("access-token-123", 3600L);

        AuthResponse response = mapper.toResponse(token);

        assertNotNull(response);
        assertEquals(token.getAccessToken(), response.accessToken());
        assertEquals(token.getExpiresIn(), response.expiresIn());
    }

    @Test
    @DisplayName("Should return null when mapping null values")
    void testNullHandling() {
        assertNull(mapper.toResponse(null));
    }
}
