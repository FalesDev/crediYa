package co.com.pragma.usecase.finduserbyiddocument;

import co.com.pragma.model.exception.EntityNotFoundException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindUserByIdDocumentUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private FindUserByIdDocumentUseCase findUserByIdDocumentUseCase;

    private User testUser;
    private final String idDocument = "12345678";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio@test.com")
                .idDocument(idDocument)
                .phoneNumber("1234567890")
                .build();
    }

    @Test
    @DisplayName("Should find user successfully by idDocument")
    void findUserByIdDocumentSuccess() {
        when(userRepository.findByIdDocument(idDocument)).thenReturn(Mono.just(testUser));

        StepVerifier.create(findUserByIdDocumentUseCase.findUserByIdDocument(idDocument))
                .expectNext(testUser)
                .verifyComplete();

        verify(customLogger).trace("Starting user search by idDocument: {}", idDocument);
        verify(customLogger).trace("User found successfully with idDocument: {}", idDocument);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found")
    void findUserByIdDocumentNotFound() {
        String nonExistentIdDocument = "99999999";
        when(userRepository.findByIdDocument(nonExistentIdDocument)).thenReturn(Mono.empty());

        StepVerifier.create(findUserByIdDocumentUseCase.findUserByIdDocument(nonExistentIdDocument))
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(customLogger).trace("Starting user search by idDocument: {}", nonExistentIdDocument);
        verify(customLogger).trace("User not found with idDocument: {}", nonExistentIdDocument);
    }

    @Test
    @DisplayName("Should propagate unexpected error when repository fails")
    void findUserUnexpectedError() {
        when(userRepository.findByIdDocument(idDocument))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(findUserByIdDocumentUseCase.findUserByIdDocument(idDocument))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("DB error"))
                .verify();

        verify(customLogger).trace("Starting user search by idDocument: {}", idDocument);
        verify(customLogger).trace(
                eq("Error searching user by idDocument: {}, error: {}"),
                eq(idDocument),
                anyString()
        );
    }
}
