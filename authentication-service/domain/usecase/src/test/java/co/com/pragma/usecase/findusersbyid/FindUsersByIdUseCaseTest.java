package co.com.pragma.usecase.findusersbyid;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FindUsersByIdUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private FindUsersByIdUseCase findUsersByIdUseCase;

    private final UUID id1 = UUID.randomUUID();
    private final UUID id2 = UUID.randomUUID();
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(id1)
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio@test.com")
                .idDocument("12345678")
                .phoneNumber("1234567890")
                .build();

        user2 = User.builder()
                .id(id2)
                .firstName("Stefano")
                .lastName("Rodriguez")
                .email("estefano@test.com")
                .idDocument("87654321")
                .phoneNumber("1234567890")
                .build();
    }

    @Test
    @DisplayName("Should return users when valid IDs are provided")
    void shouldReturnUsersWhenValidIds() {
        List<UUID> ids = List.of(id1, id2);
        when(userRepository.findByIds(ids)).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(findUsersByIdUseCase.findByIds(ids))
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(customLogger).trace("Finding users by ids: {}", ids);
        verify(customLogger).trace("Search completed for user ids: {}", ids);
        verify(userRepository).findByIds(ids);
    }

    @Test
    @DisplayName("Should return empty Flux when no users are found")
    void shouldReturnEmptyWhenNoUsersFound() {
        List<UUID> ids = List.of(id1, id2);
        when(userRepository.findByIds(ids)).thenReturn(Flux.empty());

        StepVerifier.create(findUsersByIdUseCase.findByIds(ids))
                .verifyComplete();

        verify(customLogger).trace("Finding users by ids: {}", ids);
        verify(customLogger).trace("Search completed for user ids: {}", ids);
        verify(userRepository).findByIds(ids);
    }

    @Test
    @DisplayName("Should return empty Flux when the ID list is empty")
    void shouldHandleEmptyIdList() {
        List<UUID> emptyIds = List.of();
        when(userRepository.findByIds(emptyIds)).thenReturn(Flux.empty());

        StepVerifier.create(findUsersByIdUseCase.findByIds(emptyIds))
                .verifyComplete();

        verify(customLogger).trace("Finding users by ids: {}", emptyIds);
        verify(customLogger).trace("Search completed for user ids: {}", emptyIds);
        verify(userRepository).findByIds(emptyIds);
    }

    @Test
    @DisplayName("Should log error when repository returns an error")
    void shouldLogErrorWhenRepositoryFails() {
        List<UUID> ids = List.of(id1, id2);
        RuntimeException ex = new RuntimeException("DB is down");

        when(userRepository.findByIds(ids)).thenReturn(Flux.error(ex));

        StepVerifier.create(findUsersByIdUseCase.findByIds(ids))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("DB is down"))
                .verify();

        verify(customLogger).trace("Finding users by ids: {}", ids);
        verify(customLogger).trace("Error searching for users by ids: {}, error: {}", ids, "DB is down");
        verify(userRepository).findByIds(ids);
    }
}
