package co.com.pragma.model.role.gateways;

import co.com.pragma.model.role.Role;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleRepository {
    Mono<Role> save(Role role);
    Mono<Role> findById(UUID id);
    Mono<Role> findByName(String name);
}
