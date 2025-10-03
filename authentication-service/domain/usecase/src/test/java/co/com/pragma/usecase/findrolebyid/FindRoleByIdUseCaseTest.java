package co.com.pragma.usecase.findrolebyid;

import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
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
class FindRoleByIdUseCaseTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private FindRoleByIdUseCase findRoleByIdUseCase;

    private UUID roleId;
    private Role role;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        role = new Role(roleId, "ADMIN", "Administrator role");
    }

    @Test
    @DisplayName("Should return role when it exists")
    void findRoleWhenExists() {
        when(roleRepository.findById(roleId)).thenReturn(Mono.just(role));

        StepVerifier.create(findRoleByIdUseCase.findById(roleId))
                .expectNext(role)
                .verifyComplete();

        verify(customLogger).trace("Finding role by id: {}", roleId);
        verify(customLogger).trace("Role found successfully with id: {}", roleId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when role not found")
    void findRoleWhenNotExists() {
        when(roleRepository.findById(roleId)).thenReturn(Mono.empty());

        StepVerifier.create(findRoleByIdUseCase.findById(roleId))
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(customLogger).trace("Finding role by id: {}", roleId);
        verify(customLogger).trace(
                eq("Error searching role by id: {}, error: {}"),
                eq(roleId),
                anyString()
        );
    }

    @Test
    @DisplayName("Should propagate unexpected error when repository fails")
    void findRoleUnexpectedError() {
        when(roleRepository.findById(roleId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(findRoleByIdUseCase.findById(roleId))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("DB error"))
                .verify();

        verify(customLogger).trace("Finding role by id: {}", roleId);
        verify(customLogger).trace(
                eq("Error searching role by id: {}, error: {}"),
                eq(roleId),
                anyString()
        );
    }
}

