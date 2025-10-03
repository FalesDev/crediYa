package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @InjectMocks
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private User domain;
    private UserEntity entity;

    @BeforeEach
    void setup() {
        domain = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .idDocument("99999999")
                .phoneNumber("999999999")
                .idRole(UUID.randomUUID())
                .baseSalary(5000.00)
                .password("password")
                .build();

        entity = new UserEntity(
                domain.getId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getIdDocument(),
                domain.getPhoneNumber(),
                domain.getIdRole(),
                domain.getBaseSalary(),
                domain.getPassword()
        );
    }


    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        when(repository.existsByEmail(domain.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(repositoryAdapter.existsByEmail(domain.getEmail()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmailShouldReturnFalseWhenEmailDoesNotExist() {
        String nonExistentEmail = "nonexistent@example.com";
        when(repository.existsByEmail(nonExistentEmail)).thenReturn(Mono.just(false));

        StepVerifier.create(repositoryAdapter.existsByEmail(nonExistentEmail))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when ID document exists")
    void existsByIdDocumentShouldReturnTrueWhenIdDocumentExists() {
        when(repository.existsByIdDocument(domain.getIdDocument())).thenReturn(Mono.just(true));

        StepVerifier.create(repositoryAdapter.existsByIdDocument(domain.getIdDocument()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when ID document does not exist")
    void existsByIdDocumentShouldReturnFalseWhenIdDocumentDoesNotExist() {
        String nonExistentIdDocument = "nonexistentIdDocument";
        when(repository.existsByIdDocument(nonExistentIdDocument)).thenReturn(Mono.just(false));

        StepVerifier.create(repositoryAdapter.existsByIdDocument(nonExistentIdDocument))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return user when email exists")
    void findByEmailShouldReturnUserWhenEmailExists() {
        when(repository.findByEmail(domain.getEmail())).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.findByEmail(domain.getEmail()))
                .expectNextMatches(user -> user.getEmail().equals(domain.getEmail()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmailShouldReturnEmptyWhenEmailDoesNotExist() {
        when(repository.findByEmail(domain.getEmail())).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByEmail(domain.getEmail()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return user when ID document exists")
    void findByIdDocumentShouldReturnUserWhenIdDocumentExists() {
        when(repository.findByIdDocument(domain.getIdDocument())).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.findByIdDocument(domain.getIdDocument()))
                .expectNextMatches(user -> user.getIdDocument().equals(domain.getIdDocument()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when ID document does not exist")
    void findByIdDocumentShouldReturnEmptyWhenIdDocumentDoesNotExist() {
        when(repository.findByIdDocument(domain.getIdDocument())).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByIdDocument(domain.getIdDocument()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return saved user when save succeeds")
    void saveShouldReturnSavedUser() {
        when(mapper.map(domain, UserEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.save(domain))
                .expectNextMatches(user -> user.getId().equals(domain.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when repository save fails")
    void saveShouldPropagateErrorWhenRepositoryFails() {
        RuntimeException error = new RuntimeException("DB error");
        when(mapper.map(domain, UserEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(error));

        StepVerifier.create(repositoryAdapter.save(domain))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().equals("DB error"))
                .verify();
    }

    @Test
    @DisplayName("Should return users when findByIds returns data")
    void findByIdsShouldReturnUsers() {
        List<UUID> ids = List.of(domain.getId());
        when(repository.findAllById(ids)).thenReturn(Flux.just(entity));
        when(mapper.map(entity, User.class)).thenReturn(domain);

        StepVerifier.create(repositoryAdapter.findByIds(ids))
                .expectNextMatches(user -> user.getId().equals(domain.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty Flux when findByIds returns no data")
    void findByIdsShouldReturnEmptyFlux() {
        List<UUID> ids = List.of(domain.getId());
        when(repository.findAllById(ids)).thenReturn(Flux.empty());

        StepVerifier.create(repositoryAdapter.findByIds(ids))
                .verifyComplete();
    }
}
