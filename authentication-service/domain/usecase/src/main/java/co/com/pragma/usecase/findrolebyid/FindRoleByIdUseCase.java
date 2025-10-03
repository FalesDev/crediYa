package co.com.pragma.usecase.findrolebyid;

import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class FindRoleByIdUseCase {

    private final RoleRepository roleRepository;
    private final CustomLogger customLogger;

    public Mono<Role> findById(UUID idRole) {
        customLogger.trace("Finding role by id: {}", idRole);
        return roleRepository.findById(idRole)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Role not found")))
                .doOnSuccess(role -> customLogger.trace("Role found successfully with id: {}", idRole))
                .doOnError(error -> customLogger.trace("Error searching role by id: {}, error: {}", idRole, error.getMessage()));
    }
}
