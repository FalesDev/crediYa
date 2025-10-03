package co.com.pragma.api;

import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.api.dto.request.RegisterUserRequestDto;
import co.com.pragma.api.dto.request.UserValidationRequest;
import co.com.pragma.api.dto.request.UsersFoundRequest;
import co.com.pragma.api.dto.response.AuthResponse;
import co.com.pragma.api.mapper.TokenMapper;
import co.com.pragma.api.mapper.UserMapper;
import co.com.pragma.api.service.ValidationService;
import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.exception.InvalidCredentialsException;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.token.Token;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.findrolebyid.FindRoleByIdUseCase;
import co.com.pragma.usecase.finduserbyiddocument.FindUserByIdDocumentUseCase;
import co.com.pragma.usecase.findusersbyid.FindUsersByIdUseCase;
import co.com.pragma.usecase.login.LoginUseCase;
import co.com.pragma.usecase.registeruser.RegisterUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HandlerTest {

    @Mock
    private RegisterUseCase registerUseCase;

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private FindUserByIdDocumentUseCase findUserByIdDocumentUseCase;

    @Mock
    private FindRoleByIdUseCase findRoleByIdUseCase;

    @Mock
    private FindUsersByIdUseCase findUsersByIdUseCase;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenMapper tokenMapper;

    @Mock
    private ValidationService validationService;

    @Mock
    private ServerRequest request;

    @InjectMocks
    private Handler handler;

    private RegisterUserRequestDto registerRequestDto ;
    private LoginRequest loginRequest;
    private UserValidationRequest userValidationRequest;
    private User user;
    private UserDto userDto;
    private Token token;
    private Role role;

    @BeforeEach
    void setup() {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        registerRequestDto = new RegisterUserRequestDto(
                "Fabricio",
                "Rodriguez",
                "fabricio@example.com",
                "77777777",
                "909090909",
                2500.50,
                "password123"
        );

        loginRequest = new LoginRequest("fabricio@example.com", "password123");
        userValidationRequest = new UserValidationRequest("77777777");

        user = User.builder()
                .id(userId)
                .firstName(registerRequestDto.firstName())
                .lastName(registerRequestDto.lastName())
                .email(registerRequestDto.email())
                .idDocument(registerRequestDto.idDocument())
                .phoneNumber(registerRequestDto.phoneNumber())
                .idRole(roleId)
                .baseSalary(registerRequestDto.baseSalary())
                .password(registerRequestDto.password())
                .build();

        userDto = new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getIdDocument(),
                user.getPhoneNumber(),
                user.getIdRole(),
                user.getBaseSalary(),
                user.getPassword()
        );

        token = new Token("access-token", 3600L);
        role = new Role(roleId, "ADMIN", "Administrator role");
    }

    @Test
    @DisplayName("Should register user successfully and return 201 CREATED")
    void registerUserSuccess() {
        when(request.bodyToMono(RegisterUserRequestDto.class)).thenReturn(Mono.just(registerRequestDto));
        when(validationService.validate(registerRequestDto)).thenReturn(Mono.just(registerRequestDto));
        when(userMapper.toEntity(registerRequestDto)).thenReturn(user);
        when(registerUseCase.register(user)).thenReturn(Mono.just(user));
        when(userMapper.toResponse(user)).thenReturn(userDto);

        Mono<ServerResponse> responseMono = handler.registerUser(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.statusCode()))
                .verifyComplete();

        verify(validationService).validate(registerRequestDto);
        verify(userMapper).toEntity(registerRequestDto);
        verify(registerUseCase).register(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("Should return error when register user validation fails")
    void registerUserValidationFailed() {
        when(request.bodyToMono(RegisterUserRequestDto.class)).thenReturn(Mono.just(registerRequestDto));
        when(validationService.validate(registerRequestDto)).thenReturn(Mono.error(new IllegalArgumentException("Validation failed")));

        Mono<ServerResponse> responseMono = handler.registerUser(request);

        StepVerifier.create(responseMono)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(validationService).validate(registerRequestDto);
        verifyNoInteractions(userMapper, registerUseCase);
    }

    @Test
    @DisplayName("Should login user successfully and return 200 OK")
    void loginUserSuccess() {
        AuthResponse tokenResponse = new AuthResponse("access-token", 3600L);

        when(request.bodyToMono(LoginRequest.class)).thenReturn(Mono.just(loginRequest));
        when(validationService.validate(loginRequest)).thenReturn(Mono.just(loginRequest));
        when(loginUseCase.login(loginRequest.email(), loginRequest.password())).thenReturn(Mono.just(token));
        when(tokenMapper.toResponse(token)).thenReturn(tokenResponse);

        Mono<ServerResponse> responseMono = handler.loginUser(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(validationService).validate(loginRequest);
        verify(loginUseCase).login(loginRequest.email(), loginRequest.password());
        verify(tokenMapper).toResponse(token);
    }

    @Test
    @DisplayName("Should return error when login validation fails")
    void loginUserValidationFailed() {
        when(request.bodyToMono(LoginRequest.class)).thenReturn(Mono.just(loginRequest));
        when(validationService.validate(loginRequest)).thenReturn(Mono.error(new IllegalArgumentException("Validation failed")));

        Mono<ServerResponse> responseMono = handler.loginUser(request);

        StepVerifier.create(responseMono)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(validationService).validate(loginRequest);
        verifyNoInteractions(loginUseCase, tokenMapper);
    }

    @Test
    @DisplayName("Should return error when login credentials are invalid")
    void loginUserInvalidCredentials() {
        when(request.bodyToMono(LoginRequest.class)).thenReturn(Mono.just(loginRequest));
        when(validationService.validate(loginRequest)).thenReturn(Mono.just(loginRequest));
        when(loginUseCase.login(loginRequest.email(), loginRequest.password()))
                .thenReturn(Mono.error(new InvalidCredentialsException("Invalid credentials")));

        Mono<ServerResponse> responseMono = handler.loginUser(request);

        StepVerifier.create(responseMono)
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(validationService).validate(loginRequest);
        verify(loginUseCase).login(loginRequest.email(), loginRequest.password());
        verifyNoInteractions(tokenMapper);
    }

    @Test
    @DisplayName("Should find user by id document successfully and return 200 OK")
    void findUserByIdDocumentSuccess() {
        when(request.bodyToMono(UserValidationRequest.class)).thenReturn(Mono.just(userValidationRequest));
        when(findUserByIdDocumentUseCase.findUserByIdDocument(userValidationRequest.idDocument())).thenReturn(Mono.just(user));
        when(findRoleByIdUseCase.findById(user.getIdRole())).thenReturn(Mono.just(role));

        Mono<ServerResponse> responseMono = handler.findUserByIdDocument(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(findUserByIdDocumentUseCase).findUserByIdDocument(userValidationRequest.idDocument());
        verify(findRoleByIdUseCase).findById(user.getIdRole());
    }

    @Test
    @DisplayName("Should return error when user not found by id document")
    void findUserByIdDocumentNotFound() {
        when(request.bodyToMono(UserValidationRequest.class)).thenReturn(Mono.just(userValidationRequest));
        when(findUserByIdDocumentUseCase.findUserByIdDocument(userValidationRequest.idDocument()))
                .thenReturn(Mono.error(new EntityNotFoundException("User not found")));

        Mono<ServerResponse> responseMono = handler.findUserByIdDocument(request);

        StepVerifier.create(responseMono)
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(findUserByIdDocumentUseCase).findUserByIdDocument(userValidationRequest.idDocument());
        verifyNoInteractions(findRoleByIdUseCase);
    }

    @Test
    @DisplayName("Should return error when role not found for user")
    void findUserByIdDocumentRoleNotFound() {
        when(request.bodyToMono(UserValidationRequest.class)).thenReturn(Mono.just(userValidationRequest));
        when(findUserByIdDocumentUseCase.findUserByIdDocument(userValidationRequest.idDocument())).thenReturn(Mono.just(user));
        when(findRoleByIdUseCase.findById(user.getIdRole()))
                .thenReturn(Mono.error(new EntityNotFoundException("Role not found")));

        Mono<ServerResponse> responseMono = handler.findUserByIdDocument(request);

        StepVerifier.create(responseMono)
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(findUserByIdDocumentUseCase).findUserByIdDocument(userValidationRequest.idDocument());
        verify(findRoleByIdUseCase).findById(user.getIdRole());
    }

    @Test
    @DisplayName("Should find users by IDs successfully and return 200 OK")
    void findUsersByIdSuccess() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        List<UUID> userIds = List.of(userId1, userId2);
        UsersFoundRequest usersFoundRequest = new UsersFoundRequest(userIds);

        User user1 = User.builder()
                .id(userId1)
                .firstName("User1")
                .lastName("Test")
                .email("user1@test.com")
                .idDocument("11111111")
                .baseSalary(3000.00)
                .build();

        User user2 = User.builder()
                .id(userId2)
                .firstName("User2")
                .lastName("Test")
                .email("user2@test.com")
                .idDocument("22222222")
                .baseSalary(4000.00)
                .build();

        when(request.bodyToMono(UsersFoundRequest.class)).thenReturn(Mono.just(usersFoundRequest));
        when(findUsersByIdUseCase.findByIds(userIds)).thenReturn(Flux.just(user1, user2));

        // Act & Assert
        Mono<ServerResponse> responseMono = handler.findUsersById(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                    // You might want to verify the response body content as well
                })
                .verifyComplete();

        verify(findUsersByIdUseCase).findByIds(userIds);
    }

    @Test
    @DisplayName("Should return empty list when no users found by IDs")
    void findUsersByIdEmptyResult() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        List<UUID> userIds = List.of(userId1, userId2);
        UsersFoundRequest usersFoundRequest = new UsersFoundRequest(userIds);

        when(request.bodyToMono(UsersFoundRequest.class)).thenReturn(Mono.just(usersFoundRequest));
        when(findUsersByIdUseCase.findByIds(userIds)).thenReturn(Flux.empty());

        // Act & Assert
        Mono<ServerResponse> responseMono = handler.findUsersById(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(findUsersByIdUseCase).findByIds(userIds);
    }
}
