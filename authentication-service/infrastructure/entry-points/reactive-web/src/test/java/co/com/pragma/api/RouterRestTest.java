package co.com.pragma.api;

import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.api.dto.request.RegisterUserRequestDto;
import co.com.pragma.api.dto.request.UserValidationRequest;
import co.com.pragma.api.dto.request.UsersFoundRequest;
import co.com.pragma.api.dto.response.AuthResponse;
import co.com.pragma.api.dto.response.UserFoundResponse;
import co.com.pragma.api.dto.response.UserValidationResponse;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import co.com.pragma.api.mapper.TokenMapper;
import co.com.pragma.api.mapper.UserMapper;
import co.com.pragma.api.service.ValidationService;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.token.Token;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.findrolebyid.FindRoleByIdUseCase;
import co.com.pragma.usecase.finduserbyiddocument.FindUserByIdDocumentUseCase;
import co.com.pragma.usecase.findusersbyid.FindUsersByIdUseCase;
import co.com.pragma.usecase.login.LoginUseCase;
import co.com.pragma.usecase.registeruser.RegisterUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ContextConfiguration(classes = {
        RouterRest.class,
        Handler.class,
        GlobalExceptionHandler.class
})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RegisterUseCase registerUseCase;
    @MockitoBean
    private LoginUseCase loginUseCase;
    @MockitoBean
    private FindUserByIdDocumentUseCase findUserByIdDocumentUseCase;
    @MockitoBean
    private FindUsersByIdUseCase findUsersByIdUseCase;
    @MockitoBean
    private FindRoleByIdUseCase findRoleByIdUseCase;
    @MockitoBean private UserMapper userMapper;
    @MockitoBean
    private TokenMapper tokenMapper;
    @MockitoBean private ValidationService validationService;
    @MockitoBean private CustomLogger customLogger;

    private RegisterUserRequestDto registerUserRequestDto;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        User userEntity = new User();
        userEntity.setId(UUID.randomUUID());
        userEntity.setFirstName("Fabricio");
        userEntity.setLastName("Rodriguez");
        userEntity.setEmail("fabricio@test.com");
        userEntity.setIdDocument("password123");
        userEntity.setPhoneNumber("123456789");
        userEntity.setIdRole(UUID.randomUUID());
        userEntity.setBaseSalary(3000.00);
        userEntity.setPassword("password123");

        registerUserRequestDto = new RegisterUserRequestDto(
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getEmail(),
                userEntity.getIdDocument(),
                userEntity.getPhoneNumber(),
                userEntity.getBaseSalary(),
                userEntity.getPassword()
        );

        loginRequest = new LoginRequest(
                userEntity.getEmail(),
                userEntity.getPassword()
        );

        UserDto userDto = new UserDto(
                userEntity.getId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getEmail(),
                userEntity.getIdDocument(),
                userEntity.getPhoneNumber(),
                userEntity.getIdRole(),
                userEntity.getBaseSalary(),
                userEntity.getPassword()
        );

        Token token = new Token("test-token", 3600L);
        AuthResponse authResponse = new AuthResponse("test-token", 3600L);

        Mockito.when(validationService.validate(any(RegisterUserRequestDto.class)))
                .thenReturn(Mono.just(registerUserRequestDto));

        Mockito.when(validationService.validate(any(LoginRequest.class)))
                .thenReturn(Mono.just(loginRequest));

        Mockito.when(userMapper.toEntity(any(RegisterUserRequestDto.class)))
                .thenReturn(userEntity);

        Mockito.when(userMapper.toResponse(any(User.class)))
                .thenReturn(userDto);

        Mockito.when(tokenMapper.toResponse(any(Token.class)))
                .thenReturn(authResponse);

        Mockito.when(registerUseCase.register(any(User.class)))
                .thenReturn(Mono.just(userEntity));

        Mockito.when(loginUseCase.login(any(String.class), any(String.class)))
                .thenReturn(Mono.just(token));


        Handler handler = new Handler(
                registerUseCase,
                loginUseCase,
                findUserByIdDocumentUseCase,
                findRoleByIdUseCase,
                findUsersByIdUseCase,
                userMapper,
                tokenMapper,
                validationService
        );
        webTestClient = WebTestClient.bindToRouterFunction(
                new RouterRest().routerFunction(handler, new GlobalExceptionHandler(customLogger))
        ).build();
    }

    @Test
    @DisplayName("Should return 201 Created when register request is successful")
    void testRegisterEndpointSuccess() {
        webTestClient.post()
                .uri("/auth//api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerUserRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .value(response -> {
                    Assertions.assertThat(response.email()).isEqualTo(registerUserRequestDto.email());
                    Assertions.assertThat(response.firstName()).isEqualTo(registerUserRequestDto.firstName());
                });
    }

    @Test
    @DisplayName("Should return 200 OK when login request is successful")
    void testLoginEndpointSuccess() {
        webTestClient.post()
                .uri("/auth/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    Assertions.assertThat(response.accessToken()).isEqualTo("test-token");
                    Assertions.assertThat(response.expiresIn()).isEqualTo(3600L);
                });
    }

    @Test
    @DisplayName("Should return 200 OK when findUserByIdDocument request is successful")
    void testFindUserByIdDocumentSuccess() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio@test.com")
                .idDocument("12345678")
                .phoneNumber("123456789")
                .idRole(UUID.randomUUID())
                .baseSalary(3000.00)
                .password("password123")
                .build();

        Role role = new Role(user.getIdRole(), "ADMIN", "Administrator");

        UserValidationResponse expectedResponse = new UserValidationResponse(
                user.getId(),
                user.getEmail(),
                user.getIdDocument(),
                user.getBaseSalary(),
                role.getName()
        );

        Mockito.when(findUserByIdDocumentUseCase.findUserByIdDocument(any(String.class)))
                .thenReturn(Mono.just(user));
        Mockito.when(findRoleByIdUseCase.findById(any(UUID.class)))
                .thenReturn(Mono.just(role));

        webTestClient.post()
                .uri("/auth/api/v1/users/document")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserValidationRequest("12345678"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserValidationResponse.class)
                .value(response -> {
                    Assertions.assertThat(response.idUser()).isEqualTo(expectedResponse.idUser());
                    Assertions.assertThat(response.email()).isEqualTo(expectedResponse.email());
                    Assertions.assertThat(response.idDocument()).isEqualTo(expectedResponse.idDocument());
                    Assertions.assertThat(response.role()).isEqualTo(expectedResponse.role());
                });
    }

    @Test
    @DisplayName("Should return 200 OK when findUsersById request is successful")
    void testFindUsersByIdSuccess() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UsersFoundRequest requestBody = new UsersFoundRequest(List.of(userId1, userId2));

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

        Mockito.when(findUsersByIdUseCase.findByIds(any(List.class)))
                .thenReturn(Flux.just(user1, user2));

        webTestClient.post()
                .uri("/auth/api/v1/users/find")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserFoundResponse.class)
                .hasSize(2)
                .value(users -> {
                    Assertions.assertThat(users.get(0).idUser()).isEqualTo(userId1);
                    Assertions.assertThat(users.get(1).idUser()).isEqualTo(userId2);
                });
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when unexpected exception occurs during register")
    void testRegisterUnexpectedException() {
        Mockito.when(validationService.validate(any(RegisterUserRequestDto.class)))
                .thenReturn(Mono.error(new RuntimeException()));

        webTestClient.post()
                .uri("/auth/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerUserRequestDto)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo("500");
    }
}
