package co.com.pragma.model.auth.gateways;

import reactor.core.publisher.Mono;

public interface AuthenticationGateway {
    Mono<String> login();
}
