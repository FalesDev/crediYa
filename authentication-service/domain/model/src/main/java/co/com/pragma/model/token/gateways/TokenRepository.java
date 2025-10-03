package co.com.pragma.model.token.gateways;

import co.com.pragma.model.token.Token;
import co.com.pragma.model.user.User;
import reactor.core.publisher.Mono;

public interface TokenRepository {
    Mono<Token> generateAccessToken(User user);
    Mono<User> validateToken(String token);
}
