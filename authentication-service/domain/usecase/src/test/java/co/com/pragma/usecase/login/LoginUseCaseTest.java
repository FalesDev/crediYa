package co.com.pragma.usecase.login;

import co.com.pragma.model.exception.InvalidCredentialsException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.token.Token;
import co.com.pragma.model.token.gateways.TokenRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private User user;
    private Token token;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashedPassword")
                .build();

        token = new Token("jwt-token", 3600L);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void loginSuccess() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordHasher.matches("rawPassword", user.getPassword())).thenReturn(true);
        when(tokenRepository.generateAccessToken(user)).thenReturn(Mono.just(token));

        StepVerifier.create(loginUseCase.login(user.getEmail(), "rawPassword"))
                .expectNext(token)
                .verifyComplete();

        verify(customLogger).trace("Starting login attempt for email: {}", user.getEmail());
        verify(customLogger).trace("Login successful for email: {}", user.getEmail());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void loginUserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.empty());

        StepVerifier.create(loginUseCase.login(user.getEmail(), "rawPassword"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(customLogger).trace("Login failed for email: {}", user.getEmail());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void loginWrongPassword() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordHasher.matches("wrongPassword", user.getPassword())).thenReturn(false);

        StepVerifier.create(loginUseCase.login(user.getEmail(), "wrongPassword"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(customLogger).trace("Login failed for email: {}", user.getEmail());
    }

    @Test
    @DisplayName("Should propagate error when token generation fails")
    void loginTokenGenerationFails() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordHasher.matches("rawPassword", user.getPassword())).thenReturn(true);
        when(tokenRepository.generateAccessToken(user)).thenReturn(Mono.error(new RuntimeException("Token error")));

        StepVerifier.create(loginUseCase.login(user.getEmail(), "rawPassword"))
                .expectError(RuntimeException.class)
                .verify();

        verify(customLogger).trace(
                eq("Login process failed for email: {}: {}"),
                eq(user.getEmail()),
                anyString()
        );
    }
}
