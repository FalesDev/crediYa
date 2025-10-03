package co.com.pragma.usecase.finduserbyiddocument;

import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FindUserByIdDocumentUseCase {

    private final UserRepository userRepository;
    private final CustomLogger customLogger;

    public Mono<User> findUserByIdDocument(String idDocument) {
        customLogger.trace("Starting user search by idDocument: {}", idDocument);

        return userRepository.findByIdDocument(idDocument)
                .switchIfEmpty(Mono.defer(() -> {
                    customLogger.trace("User not found with idDocument: {}", idDocument);
                    return Mono.error(new EntityNotFoundException("User not found by IdDocument"));
                }))
                .doOnSuccess(user -> customLogger.trace("User found successfully with idDocument: {}", idDocument))
                .doOnError(error -> customLogger.trace("Error searching user by idDocument: {}, error: {}", idDocument, error.getMessage()));
    }
}
