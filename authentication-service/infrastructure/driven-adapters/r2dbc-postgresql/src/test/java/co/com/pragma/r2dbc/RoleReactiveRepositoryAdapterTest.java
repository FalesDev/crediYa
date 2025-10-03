package co.com.pragma.r2dbc;

import co.com.pragma.model.role.Role;
import co.com.pragma.r2dbc.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleReactiveRepositoryAdapterTest {

    @InjectMocks
    RoleReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    RoleReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private Role domain;
    private RoleEntity entity;

    @BeforeEach
    void setup() {
        domain = Role.builder()
                .id(UUID.randomUUID())
                .name("CLIENT")
                .description("description")
                .build();

        entity = new RoleEntity(
                domain.getId(),
                domain.getName(),
                domain.getDescription()
        );
    }

    @Test
    @DisplayName("Should return role when name exists")
    void findByNameShouldReturnRoleWhenNameExists() {
        when(repository.findByName(domain.getName())).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Role.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.findByName(domain.getName()))
                .expectNextMatches(role -> role.getName().equals(domain.getName()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when name does not exist")
    void findByNameShouldReturnEmptyWhenNameDoesNotExist() {
        when(repository.findByName(domain.getName())).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByName(domain.getName()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save role successfully")
    void saveShouldReturnSavedRole() {
        when(mapper.map(domain, RoleEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Role.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.save(domain))
                .expectNextMatches(role -> role.equals(domain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find role by id when exists")
    void findByIdShouldReturnRoleWhenExists() {
        when(repository.findById(domain.getId())).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Role.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.findById(domain.getId()))
                .expectNextMatches(role -> role.getId().equals(domain.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when role id does not exist")
    void findByIdShouldReturnEmptyWhenNotExists() {
        when(repository.findById(domain.getId())).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findById(domain.getId()))
                .verifyComplete();
    }
}
