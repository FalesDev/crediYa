package co.com.pragma.api;

import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.api.dto.request.RegisterUserRequestDto;
import co.com.pragma.api.dto.request.UserValidationRequest;
import co.com.pragma.api.dto.request.UsersFoundRequest;
import co.com.pragma.api.dto.response.UserValidationResponse;
import co.com.pragma.api.dto.response.UserFoundResponse;
import co.com.pragma.api.mapper.TokenMapper;
import co.com.pragma.api.mapper.UserMapper;
import co.com.pragma.api.service.ValidationService;
import co.com.pragma.usecase.findrolebyid.FindRoleByIdUseCase;
import co.com.pragma.usecase.findusersbyid.FindUsersByIdUseCase;
import co.com.pragma.usecase.finduserbyiddocument.FindUserByIdDocumentUseCase;
import co.com.pragma.usecase.login.LoginUseCase;
import co.com.pragma.usecase.registeruser.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final FindUserByIdDocumentUseCase findUserByIdDocumentUseCase;
    private final FindRoleByIdUseCase findRoleByIdUseCase;
    private final FindUsersByIdUseCase findUsersByIdUseCase;
    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final ValidationService validationService;

    public Mono<ServerResponse> registerUser(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequestDto.class)
                .flatMap(validationService::validate)
                .map(userMapper::toEntity)
                .flatMap(registerUseCase::register)
                .map(userMapper::toResponse)
                .flatMap(dto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto)
                );
    }

    public Mono<ServerResponse> loginUser(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(validationService::validate)
                .flatMap(req -> loginUseCase.login(req.email(), req.password()))
                .map(tokenMapper::toResponse)
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res)
                );

    }

    public Mono<ServerResponse> findUserByIdDocument(ServerRequest request) {
        return request.bodyToMono(UserValidationRequest.class)
                .flatMap(validationRequest ->
                        findUserByIdDocumentUseCase.findUserByIdDocument(validationRequest.idDocument())
                )
                .flatMap(user ->
                        findRoleByIdUseCase.findById(user.getIdRole())
                                .map(role -> new UserValidationResponse(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getIdDocument(),
                                        user.getBaseSalary(),
                                        role.getName()))
                )
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                );
    }

    public Mono<ServerResponse> findUsersById(ServerRequest request) {
        return request.bodyToMono(UsersFoundRequest.class)
                .flatMapMany(req -> findUsersByIdUseCase.findByIds(req.userIds()))
                .map(user -> new UserFoundResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getIdDocument(),
                        user.getBaseSalary()
                ))
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list)
                );
    }
}
