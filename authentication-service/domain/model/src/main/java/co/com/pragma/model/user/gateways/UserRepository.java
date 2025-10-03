package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserRepository {

    Mono<User> save(User user);
    Flux<User> findByIds(List<UUID> userIds);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByIdDocument(String idDocument);
    Mono<User> findByEmail(String email);
    Mono<User> findByIdDocument(String idDocument);
}
