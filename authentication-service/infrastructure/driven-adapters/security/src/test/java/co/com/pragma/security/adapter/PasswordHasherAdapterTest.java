package co.com.pragma.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordHasherAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordEncoderAdapter adapter;

    private String rawPassword;
    private String encodedPassword;

    @BeforeEach
    void setup() {
        rawPassword = "mySecret123";
        encodedPassword = "$2a$10$abcdefg1234567";
    }

    @Test
    @DisplayName("Should encode password using Spring Security encoder")
    void testEncode() {
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = adapter.encode(rawPassword);

        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("Should return true when raw password matches encoded password")
    void testMatchesTrue() {
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = adapter.matches(rawPassword, encodedPassword);

        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should return false when raw password does not match encoded password")
    void testMatchesFalse() {
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = adapter.matches(rawPassword, encodedPassword);

        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}
