package co.com.pragma.usecase.login;

import co.com.pragma.model.exception.InvalidCredentialsException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.token.Token;
import co.com.pragma.model.token.gateways.TokenRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenRepository tokenRepository;
    private final CustomLogger customLogger;

    public Mono<Token> login(String email, String rawPassword) {
        customLogger.trace("Starting login attempt for email: {}", email);

        return userRepository.findByEmail(email)
                .filter(user -> passwordHasher.matches(rawPassword, user.getPassword()))
                .switchIfEmpty(Mono.defer(() -> {
                    customLogger.trace("Login failed for email: {}", email);
                    return Mono.error(new InvalidCredentialsException("Invalid credentials"));
                }))
                .flatMap(tokenRepository::generateAccessToken)
                .doOnSuccess(token -> customLogger.trace("Login successful for email: {}", email))
                .doOnError(error -> customLogger.trace("Login process failed for email: {}: {}", email, error.getMessage()));
    }
}
