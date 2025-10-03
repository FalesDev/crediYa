package co.com.pragma.usecase.findusersbyid;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FindUsersByIdUseCase {

    private final UserRepository userRepository;
    private final CustomLogger customLogger;

    public Flux<User> findByIds(List<UUID> idUsers) {
        customLogger.trace("Finding users by ids: {}", idUsers);
        return userRepository.findByIds(idUsers)
                .doOnComplete(() -> customLogger.trace("Search completed for user ids: {}", idUsers))
                .doOnError(error -> customLogger.trace("Error searching for users by ids: {}, error: {}", idUsers, error.getMessage()));
    }
}
