package co.com.pragma.usecase.registeruser;

import co.com.pragma.model.exception.EmailAlreadyExistsException;
import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.exception.IdDocumentAlreadyExistsException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.gateways.TransactionManager;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private CustomLogger customLogger;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    private User testUser;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio@example.com")
                .idDocument("77777777")
                .phoneNumber("909090909")
                .baseSalary(2500.50)
                .password("plainPassword")
                .build();

        clientRole = Role.builder()
                .id(UUID.randomUUID())
                .name("CLIENT")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully when email is available and role exists")
    void registerSuccess() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.existsByIdDocument(testUser.getIdDocument())).thenReturn(Mono.just(false));
        when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(clientRole));
        when(passwordHasher.encode("plainPassword")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        StepVerifier.create(registerUseCase.register(testUser))
                .expectNextMatches(user ->
                        user.getEmail().equals("fabricio@example.com") &&
                                user.getPassword().equals("encodedPassword") &&
                                user.getIdRole().equals(clientRole.getId())
                )
                .verifyComplete();

        verify(userRepository).existsByEmail("fabricio@example.com");
        verify(userRepository).existsByIdDocument("77777777");
        verify(roleRepository).findByName("CLIENT");
        verify(passwordHasher).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email is already registered")
    void registerEmailAlreadyExists() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(true));
        when(userRepository.existsByIdDocument(testUser.getIdDocument())).thenReturn(Mono.just(false));
        when(roleRepository.findByName(anyString())).thenReturn(Mono.empty());

        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        StepVerifier.create(registerUseCase.register(testUser))
                .expectError(EmailAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByEmail("fabricio@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IdDocumentAlreadyExistsException when ID document is already registered")
    void registerIdDocumentAlreadyExists() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.existsByIdDocument(testUser.getIdDocument())).thenReturn(Mono.just(true));
        when(roleRepository.findByName(anyString())).thenReturn(Mono.empty());

        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        StepVerifier.create(registerUseCase.register(testUser))
                .expectError(IdDocumentAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByEmail("fabricio@example.com");
        verify(userRepository).existsByIdDocument("77777777");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when role CLIENT does not exist")
    void registerRoleNotFound() {
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.existsByIdDocument(testUser.getIdDocument())).thenReturn(Mono.just(false));
        when(roleRepository.findByName("CLIENT")).thenReturn(Mono.empty());
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        StepVerifier.create(registerUseCase.register(testUser))
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(userRepository).existsByEmail("fabricio@example.com");
        verify(userRepository).existsByIdDocument("77777777");
        verify(roleRepository).findByName("CLIENT");
        verify(userRepository, never()).save(any());
    }
}
